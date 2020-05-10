package com.raindrop.anno;

import java.lang.annotation.*;

/**
 * @name: com.raindrop.anno.WebLogging.java
 * @description: Interface description annotation
 * @author: Wang Liang
 * @create Time: 2018/6/13 16:15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLogging {

    /**
     * Web interface description
     */
    String value() default "";

}
