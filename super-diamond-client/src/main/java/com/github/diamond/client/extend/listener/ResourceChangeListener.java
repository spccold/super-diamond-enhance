package com.github.diamond.client.extend.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.diamond.client.event.ConfigurationEvent;
import com.github.diamond.client.event.ConfigurationListener;
import com.github.diamond.client.extend.resources.DistributedResourceManagerImpl.ResourceHolder;
import com.github.diamond.client.extend.resources.DistributedResourcesHolder;
import com.github.diamond.client.extend.utils.StringConverter;

/**
 * DRM监听器
 * @author kanguangwen
 *
 */
public class ResourceChangeListener implements ConfigurationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceChangeListener.class);

    @Override
    public void configurationChanged(ConfigurationEvent event) {
        String propertyName = event.getPropertyName();
        Object newValue = event.getPropertyValue();
        ResourceHolder holder = DistributedResourcesHolder.getResource(propertyName);
        if (null != holder) {
            Object impl = holder.getImpl();
            Object convertValue = StringConverter.convertObjectFromString(holder.getMethodParameterType(),
                (String) newValue);
            try {
                //如果beforeUpdateMethod存在，则执行
                Method beforeUpdateMethod = holder.getBeforeUpdateMethod();
                if (null != beforeUpdateMethod) {
                    beforeUpdateMethod.invoke(impl, propertyName, convertValue);
                }
                //执行属性动态变更
                holder.getMethod().invoke(impl, convertValue);
                LOGGER.info("[key:" + propertyName + "] update at "
                            + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                //如果afterUpdateMethod存在，则执行
                Method afterUpdateMethod = holder.getAfterUpdateMethod();
                if (null != afterUpdateMethod) {
                    afterUpdateMethod.invoke(impl, propertyName, convertValue);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
