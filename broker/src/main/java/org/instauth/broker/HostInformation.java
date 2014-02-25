package org.instauth.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostInformation {

	private static final Logger LOG = LoggerFactory.getLogger(HostInformation.class);

	private String charmName;

	
	public String getCharmName() {
		return charmName;
	}

	public void setCharmName(String charmName) {
		LOG.info("Changing image tag to '{}'.", charmName);
		this.charmName = charmName;
	}

}
