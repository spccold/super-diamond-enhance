package com.github.diamond.client.extend.init;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

import com.github.diamond.client.extend.DRMClient;
import com.github.diamond.client.extend.annotation.DResource;

public class DRMInitBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //eager initialize DRM
        //beanFactory.getBean(PropertiesConfigurationFactoryBean.class);
        //eager init all drm resources
        Map<String, Object> resourcesBeanMap = beanFactory.getBeansWithAnnotation(DResource.class);
        //register all drm resources
        for (Entry<String, Object> entry : resourcesBeanMap.entrySet()) {
            DRMClient.getManager().register(entry.getValue());
        }
    }

    @Override
    public int getOrder() {
        //该BeanFactoryPostProcessor在Ordered级别下具有最高优先级
        return Ordered.LOWEST_PRECEDENCE;
    }

}
