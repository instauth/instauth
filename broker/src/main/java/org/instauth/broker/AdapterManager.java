package org.instauth.broker;

public interface AdapterManager {

	/**
	 * Downloads and installs the adapter required for the host charm.
	 * @param hostCharmName Name of the host charm.
	 */
	public void downloadAndInstallAdapter(String hostCharmName);

	public void configureHost();

	public void unconfigureHost();

	public void uninstallAdapter();
}
