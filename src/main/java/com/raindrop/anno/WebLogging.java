package com.raindrop.anno;

import java.lang.annotation.*;

/**
 * Created by wangliang on 2018/6/13.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebLogging {

	String description() default "";

}
