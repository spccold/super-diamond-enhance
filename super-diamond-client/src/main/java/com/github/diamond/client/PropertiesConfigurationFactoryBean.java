/**        
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.    
 */
package com.github.diamond.client;

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

import com.github.diamond.client.event.ConfigurationListener;
import com.google.common.base.Preconditions;

/**
 * Create on @2013-8-26 @上午9:29:52 
 * @author bsli@ustcinfo.com
 */
public class PropertiesConfigurationFactoryBean implements FactoryBean<Properties> {
    //properties必须位于业务系统的classpath的spring目录下
    private static final String            RESOURCE_LOCATION       = "spring/diamond.properties";

    private static final Logger            LOGGER                  = LoggerFactory
                                                                       .getLogger(PropertiesConfigurationFactoryBean.class);
    private static PropertiesConfiguration __configuration;

    private static boolean                 init                    = false;

    //netty 默认域名
    //TODO 使用方自己申明
    private static final String            DEFAULT_NETTY_HA_DOMAIN = "xxx";

    //netty 默认端口
    //TODO 使用方自己申明
    private static final int               DEFAULT_NETTY_HA_PORT   = 0000;

    public PropertiesConfigurationFactoryBean() {
        this(null);
    }

    /* public PropertiesConfigurationFactoryBean(List<ConfigurationListener> listeners) {
         init = true;
         __configuration = new PropertiesConfiguration();

         if (listeners != null) {
             for (ConfigurationListener listener : listeners) {
                 __configuration.addConfigurationListener(listener);
             }
         }
     }*/

    //add by 机冷
    public PropertiesConfigurationFactoryBean(List<ConfigurationListener> listeners) {
        if (!init) {//防止重复初始化
            init = true;
            try {
                ClassLoader cl = PropertiesConfigurationFactoryBean.class.getClassLoader();
                String diamondHost = null;
                int diamondPort = 0;
                String diamondProjcode = null;
                String diamondProfile = null;
                String diamondModules = null;
                //加载diamond.properties
                Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(RESOURCE_LOCATION) : ClassLoader
                    .getSystemResources(RESOURCE_LOCATION));
                Set<URL> result = new LinkedHashSet<URL>(16);
                while (resourceUrls.hasMoreElements()) {
                    URL url = resourceUrls.nextElement();
                    result.add(url);
                }
                if (result.size() == 0) {
                    throw new RuntimeException("没在classpath下发现diamond.properties文件,请创建配置文件");
                }
                if (result.size() > 1) {
                    throw new RuntimeException("在classpath下发现多个diamond.properties文件,请删除多余的配置文件");
                }
                Properties properties = new Properties();
                URL resourceUrl = result.toArray(new URL[result.size()])[0];
                properties.load(resourceUrl.openStream());
                diamondHost = (String) properties.get("cs.diamondHost");
                if (StringUtils.isBlank(diamondHost)) {
                    diamondHost = DEFAULT_NETTY_HA_DOMAIN;
                }
                if (null == properties.get("cs.diamondPort")) {
                    diamondPort = DEFAULT_NETTY_HA_PORT;
                } else {
                    diamondPort = Integer.parseInt((String) properties.get("cs.diamondPort"));
                }

                diamondProfile = (String) properties.get("cs.diamondProfile");
                diamondProjcode = (String) properties.get("cs.diamondProjcode");
                diamondModules = (String) properties.get("cs.diamondModules");

                Preconditions.checkArgument(StringUtils.isNotBlank(diamondHost), "diamondHost is null or empty");
                Preconditions.checkArgument(StringUtils.isNotBlank(diamondProfile), "diamondProfile is null or empty");
                Preconditions
                    .checkArgument(StringUtils.isNotBlank(diamondProjcode), "diamondProjcode is null or empty");
                __configuration = new PropertiesConfiguration(diamondHost, diamondPort, diamondProjcode,
                    diamondProfile, diamondModules);

                if (listeners != null) {
                    for (ConfigurationListener listener : listeners) {
                        __configuration.addConfigurationListener(listener);
                    }
                }
            } catch (Exception e) {
                init = false;
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    public PropertiesConfigurationFactoryBean(final String projCode, final String profile, final String modules) {
        this(projCode, profile, modules, null);
    }

    public PropertiesConfigurationFactoryBean(final String projCode, final String profile, final String modules,
                                              List<ConfigurationListener> listeners) {
        init = true;
        __configuration = new PropertiesConfiguration(projCode, profile);

        if (listeners != null) {
            for (ConfigurationListener listener : listeners) {
                __configuration.addConfigurationListener(listener);
            }
        }
    }

    public PropertiesConfigurationFactoryBean(String host, int port, final String projCode, final String profile,
                                              final String modules) {
        this(host, port, projCode, profile, modules, null);
    }

    public PropertiesConfigurationFactoryBean(String host, int port, final String projCode, final String profile,
                                              final String modules, List<ConfigurationListener> listeners) {
        init = true;
        __configuration = new PropertiesConfiguration(host, port, projCode, profile, modules);

        if (listeners != null) {
            for (ConfigurationListener listener : listeners) {
                __configuration.addConfigurationListener(listener);
            }
        }
    }

    @Override
    public Properties getObject() throws Exception {
        Assert.notNull(__configuration);
        return __configuration.getProperties();
    }

    @Override
    public Class<?> getObjectType() {
        return Properties.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public static PropertiesConfiguration getPropertiesConfiguration() {
        if (!init) {
            throw new ConfigurationRuntimeException("PropertiesConfigurationFactoryBean 没有初始化");
        }
        return __configuration;
    }

    /**
     * 当servlet容器关闭时,关闭netty client
     */
    public void close() {
        __configuration.close();
    }
}
