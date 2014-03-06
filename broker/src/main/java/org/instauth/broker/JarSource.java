package org.instauth.broker;

import java.io.File;

public interface JarSource {

    /**
     * Downloads a JAR with the given Maven coordinates. Return a (temporary, cached) File containing the JAR
     * if the download was successful, otherwise throws an {@link IllegalArgumentException} (details TBD).
     *
     * How to map from Juju relation to Maven coordinates?
     * Note that the organsation, artifactId, and version are specific to the JAR and not to the corresponding
     * SSO Client. Perhaps we pass in just the major/minor version components and let the JarSource somehow
     * determine the latest micro/qualifier version components so we automatically get bug fixes?
     */
    File download(String groupId, String artifactId, String version);

}
