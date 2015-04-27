package com.github.diamond.client.extend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注了此注解的方法，将在资源属性更新后被调用。供使用者加入自定义的通用处理逻辑。
 * 此注解是可选的。
 * 
 * @author kanguangwen
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterUpdate {

}
