package com.github.diamond.client.extend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识DRM资源
 * 
 * @author kanguangwen
 * @version $Id: DResource.java, v 0.1 2015年2月25日 下午3:46:13 kanguangwen Exp $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value=ElementType.TYPE)
@Documented
public @interface DResource {
}
