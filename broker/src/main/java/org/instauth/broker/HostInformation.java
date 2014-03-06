package org.instauth.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostInformation {

	private static final Logger LOG = LoggerFactory.getLogger(HostInformation.class);

	private String charmName;

	private AdapterManager adapterManager;
	
    public void setAdapterManager(AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

	public String getCharmName() {
		return charmName;
	}

	public void setCharmName(String charmName) {
		LOG.info("Changing charm name to '{}'.", charmName);
		this.charmName = charmName;

		if (this.adapterManager == null) {
		    // TODO - can we avoid this scenario?
            LOG.warn("AdapterManager not injected yet.");
            return;
        }
		try {
            this.adapterManager.downloadAndInstallAdapter(charmName);
        } catch (Exception e) {
            LOG.error("Failed to download and install adapter for (host) charm '{}'", charmName, e);
        }
	}

}
