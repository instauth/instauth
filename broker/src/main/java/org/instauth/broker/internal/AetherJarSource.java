package org.instauth.broker.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.collection.DependencyTraverser;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.util.artifact.DefaultArtifactTypeRegistry;
import org.eclipse.aether.util.graph.manager.ClassicDependencyManager;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaDependencyContextRefiner;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;
import org.eclipse.aether.util.graph.transformer.NearestVersionSelector;
import org.eclipse.aether.util.graph.transformer.SimpleOptionalitySelector;
import org.eclipse.aether.util.graph.traverser.FatArtifactTraverser;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.eclipse.aether.version.Version;
import org.instauth.broker.JarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AetherJarSource implements JarSource {

    private static final Logger LOG = LoggerFactory.getLogger(AetherJarSource.class);

    private RepositorySystem repositorySystem;

    public void setRepositorySystem(RepositorySystem repositorySystem) {
        this.repositorySystem = repositorySystem;
    }

    /**
     * Creates a new Maven-like repository system session by initializing the session with values typical for
     * Maven-based resolution. In more detail, this method configures settings relevant for the processing of dependency
     * graphs, most other settings remain at their generic default value. Use the various setters to further configure
     * the session with authentication, mirror, proxy and other information required for your environment.
     * 
     * @return The new repository system session, never {@code null}.
     */
    private DefaultRepositorySystemSession newSession() {
        // TODO - Determine the minimum required settings for the repository system session
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

        DependencyTraverser depTraverser = new FatArtifactTraverser();
        session.setDependencyTraverser(depTraverser);

        DependencyManager depManager = new ClassicDependencyManager();
        session.setDependencyManager(depManager);

        DependencySelector depFilter = new AndDependencySelector(new ScopeDependencySelector("test", "provided"), new OptionalDependencySelector(),
            new ExclusionDependencySelector());
        session.setDependencySelector(depFilter);

        DependencyGraphTransformer transformer = new ConflictResolver(new NearestVersionSelector(), new JavaScopeSelector(),
            new SimpleOptionalitySelector(), new JavaScopeDeriver());
        new ChainedDependencyGraphTransformer(transformer, new JavaDependencyContextRefiner());
        session.setDependencyGraphTransformer(transformer);

        DefaultArtifactTypeRegistry stereotypes = new DefaultArtifactTypeRegistry();
        stereotypes.add(new DefaultArtifactType("pom"));
        stereotypes.add(new DefaultArtifactType("maven-plugin", "jar", "", "java"));
        stereotypes.add(new DefaultArtifactType("jar", "jar", "", "java"));
        stereotypes.add(new DefaultArtifactType("ejb", "jar", "", "java"));
        stereotypes.add(new DefaultArtifactType("ejb-client", "jar", "client", "java"));
        stereotypes.add(new DefaultArtifactType("test-jar", "jar", "tests", "java"));
        stereotypes.add(new DefaultArtifactType("javadoc", "jar", "javadoc", "java"));
        stereotypes.add(new DefaultArtifactType("java-source", "jar", "sources", "java", false, false));
        stereotypes.add(new DefaultArtifactType("war", "war", "", "java", false, true));
        stereotypes.add(new DefaultArtifactType("ear", "ear", "", "java", false, true));
        stereotypes.add(new DefaultArtifactType("rar", "rar", "", "java", false, true));
        stereotypes.add(new DefaultArtifactType("par", "par", "", "java", false, true));
        session.setArtifactTypeRegistry(stereotypes);

        session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(true, true));

        Properties sysProps = System.getProperties();
        session.setSystemProperties(sysProps);
        session.setConfigProperties(sysProps);

        return session;
    }

    private RepositorySystemSession newRepositorySystemSession() {
        DefaultRepositorySystemSession session = newSession();
        // TODO - determine where to put the local repository - the settings below will use ${VIRGO_HOME}/target/local-repo
        LocalRepository localRepository = new LocalRepository("target/local-repo");
        LocalRepositoryManager localRepositoryManager = repositorySystem.newLocalRepositoryManager(session, localRepository);
        session.setLocalRepositoryManager(localRepositoryManager);
        session.setTransferListener(new ConsoleTransferListener());
        session.setRepositoryListener(new ConsoleRepositoryListener());
        return session;
    }

    @Override
    public File download(String groupId, String artifactId, String version) {
        LOG.info("Resolving latest version of the adapter for (host) charm '{}'", artifactId);
        RepositorySystemSession session = newRepositorySystemSession();

        RemoteRepository remoteRepository = new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/").build();
        ArrayList<RemoteRepository> remoteRepositoryList = new ArrayList<RemoteRepository>(Arrays.asList(remoteRepository));

        Artifact artifact = new DefaultArtifact(groupId, artifactId, "jar", version);
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(remoteRepositoryList);

        VersionRangeResult rangeResult;
        try {
            rangeResult = repositorySystem.resolveVersionRange(session, rangeRequest);
            Version newestVersion = rangeResult.getHighestVersion();
            System.out.println("Newest version " + newestVersion + " from repository " + rangeResult.getRepository(newestVersion));
            LOG.info("Resolved version of adapter for (host) charm '{}' to '{}'", artifactId, newestVersion);
        } catch (VersionRangeResolutionException e) {
            LOG.error("Failed to resolve the version range of the adapter for (host) charm '{}'", artifactId, e);
            throw new IllegalArgumentException("Failed to resolve version range of the adapter.");
        }

        Artifact latestArtifact = new DefaultArtifact( "org.eclipse.aether:aether-util:" + rangeResult.getHighestVersion().toString());
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact( latestArtifact );
        artifactRequest.setRepositories( remoteRepositoryList );

        ArtifactResult artifactResult;
        try {
            artifactResult = repositorySystem.resolveArtifact( session, artifactRequest );
        } catch (ArtifactResolutionException e) {
            LOG.error("Failed to resolve the newest version of the adapter for (host) charm '{}'", artifactId, e);
            throw new IllegalArgumentException("Failed to resolve the newest version of the adapter.");
        }

        artifact = artifactResult.getArtifact();
        LOG.info("Artifact resolved to '{}'", artifact.getFile());

        return artifact.getFile();
    }

}
