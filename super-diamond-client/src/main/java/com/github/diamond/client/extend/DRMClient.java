package com.github.diamond.client.extend;

import com.github.diamond.client.extend.resources.DistributedResourceManagerImpl;

/**
 * singleton
 * 
 * @author kanguangwen
 *
 */
public class DRMClient {

	private static final DistributedResourceManagerImpl MANAGER = new DistributedResourceManagerImpl();

	private DRMClient() {
	};

	public static DistributedResourceManagerImpl getManager() {
		return MANAGER;
	}
}
