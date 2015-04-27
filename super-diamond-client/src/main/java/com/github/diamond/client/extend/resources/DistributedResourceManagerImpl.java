package com.github.diamond.client.extend.resources;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.BeanUtils;

import com.github.diamond.client.PropertiesConfigurationFactoryBean;
import com.github.diamond.client.extend.annotation.AfterUpdate;
import com.github.diamond.client.extend.annotation.BeforeUpdate;
import com.github.diamond.client.extend.annotation.DAttribute;
import com.github.diamond.client.extend.utils.StringConverter;

public class DistributedResourceManagerImpl {

    private static final Properties initP = PropertiesConfigurationFactoryBean.getPropertiesConfiguration()
                                              .getProperties();

    /**
     * 注册资源
     * 
     * @param resourceObj
     */
    public void register(Object resourceObj) {
        Class<?> targetClass = resourceObj.getClass();

        //获取beforeUpdateMethod和afterUpdateMethod
        Method[] methods = targetClass.getDeclaredMethods();
        Method beforeUpdateMethod = null;
        Method afterUpdateMethod = null;

        for (Method method : methods) {
            BeforeUpdate beforeAnn = method.getAnnotation(BeforeUpdate.class);
            if (beforeAnn != null) {
                // check参数
                validateCallbackMethodParams(method);
                beforeUpdateMethod = method;
            }
            AfterUpdate afterAnn = method.getAnnotation(AfterUpdate.class);
            if (afterAnn != null) {
                // check参数
                validateCallbackMethodParams(method);
                afterUpdateMethod = method;
            }
        }

        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(targetClass);
        Method writeMethod = null;
        Method readMethod = null;
        DAttribute attr = null;
        Class<?> propertyType = null;
        if (ArrayUtils.isNotEmpty(descriptors)) {
            for (PropertyDescriptor descriptor : descriptors) {
                String propertyName = descriptor.getName();

                propertyType = descriptor.getPropertyType();
                try {
                    attr = getAnnotation(targetClass, propertyName);
                    if (null == attr) {
                        continue;
                    }

                    String key = attr.key();

                    writeMethod = descriptor.getWriteMethod();
                    readMethod = descriptor.getReadMethod();
                    if (null == writeMethod || null == readMethod) {
                        throw new RuntimeException(resourceObj.getClass().getName() + "不是标准的JavaBean!");
                    }
                    // 执行资源的初始化
                    if (initP.containsKey(key)) {
                        writeMethod.invoke(resourceObj,
                            StringConverter.convertObjectFromString(propertyType, (String) initP.get(key)));
                    } else {
                        throw new RuntimeException(resourceObj.getClass().getSimpleName() + "的@DAttribute(key = \""
                                                   + key + "\")未在配置中心进行配置");
                    }
                    // 添加资源到资源池,用于后期动态变动资源
                    ResourceHolder holder = new ResourceHolder();
                    holder.setImpl(resourceObj);
                    holder.setMethod(writeMethod);
                    holder.setMethodParameterType(propertyType);
                    holder.setBeforeUpdateMethod(beforeUpdateMethod);
                    holder.setAfterUpdateMethod(afterUpdateMethod);

                    DistributedResourcesHolder.addResource(key, holder);
                } catch (SecurityException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private DAttribute getAnnotation(Class<?> targetClass, String fieldName) {
        Field f;
        try {
            f = targetClass.getDeclaredField(fieldName);
        } catch (Exception e) {
            return null;
        }
        // 检查字段上有没有DAttribute注解
        return f.getAnnotation(DAttribute.class);

    }

    /**
     * 检查回调方法的参数类型。
     * 
     * @param callbackMethod
     */
    void validateCallbackMethodParams(Method callbackMethod) {
        Class<?>[] paramClazzs = callbackMethod.getParameterTypes();
        if (!(paramClazzs.length == 2 && paramClazzs[0].equals(String.class) && paramClazzs[1].equals(Object.class))) {
            throw new RuntimeException("回调方法[" + callbackMethod.getName() + "]参数类型必须为(String key,Object newValue)");
        }
    }

    /**
     * DRM属性 资源持有器
     * 
     * @author kanguangwen
     *
     */
    public class ResourceHolder {
        /**属性所处对象*/
        private Object   impl;
        /**属性对应的setter方法*/
        private Method   method;
        /**属性对应setter方法的参数类型*/
        private Class<?> methodParameterType;
        /**属性变更之前的方法*/
        private Method   beforeUpdateMethod;
        /**属性变更之后的方法*/
        private Method   afterUpdateMethod;

        public Object getImpl() {
            return impl;
        }

        public void setImpl(Object impl) {
            this.impl = impl;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Class<?> getMethodParameterType() {
            return methodParameterType;
        }

        public void setMethodParameterType(Class<?> methodParameterType) {
            this.methodParameterType = methodParameterType;
        }

        public Method getBeforeUpdateMethod() {
            return beforeUpdateMethod;
        }

        public void setBeforeUpdateMethod(Method beforeUpdateMethod) {
            this.beforeUpdateMethod = beforeUpdateMethod;
        }

        public Method getAfterUpdateMethod() {
            return afterUpdateMethod;
        }

        public void setAfterUpdateMethod(Method afterUpdateMethod) {
            this.afterUpdateMethod = afterUpdateMethod;
        }
    }

}
