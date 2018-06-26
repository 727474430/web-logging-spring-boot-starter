package com.raindrop.anno;

import java.lang.annotation.*;

/**
 * @name: com.raindrop.anno.WebLogging.java
 * @description: 接口描述注解
 * @author: Wang Liang 
 * @create Time: 2018/6/13 16:15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLogging {

	String description() default "";

}
