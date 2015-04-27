package com.github.diamond.client.extend.resources;

import java.util.HashMap;
import java.util.Map;

import com.github.diamond.client.extend.resources.DistributedResourceManagerImpl.ResourceHolder;

/**
 * 分布式资源池
 * @author kanguangwen
 *
 */
public class DistributedResourcesHolder {
    private static final Map<String, ResourceHolder> RESOURCE_MAP = new HashMap<String, ResourceHolder>();

    /**
     * 添加资源 
     * @param key
     * @param writeMethod
     */
    public static void addResource(String key, ResourceHolder resource) {
        RESOURCE_MAP.put(key, resource);
    }

    public static ResourceHolder getResource(String key) {
        return RESOURCE_MAP.get(key);
    }
}
