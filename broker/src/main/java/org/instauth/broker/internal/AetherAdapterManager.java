
package org.instauth.broker.internal;

import java.io.File;

import org.instauth.broker.AdapterManager;
import org.instauth.broker.JarSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AetherAdapterManager implements AdapterManager {

    private static final Logger LOG = LoggerFactory.getLogger(AetherAdapterManager.class);

    private JarSource jarSource;

    public void setJarSource(JarSource jarSource) {
        this.jarSource = jarSource;
    }

    public void init() {
    }

    @Override
    public void downloadAndInstallAdapter(String hostCharmName) {
        File adapterJarFile = this.jarSource.download("org.eclipse.aether", "aether-util", "[0,)");
    }

    @Override
    public void configureHost() {
        // TODO Auto-generated method stub
    }

    @Override
    public void unconfigureHost() {
        // TODO Auto-generated method stub
    }

    @Override
    public void uninstallAdapter() {
        // TODO Auto-generated method stub
    }

}
