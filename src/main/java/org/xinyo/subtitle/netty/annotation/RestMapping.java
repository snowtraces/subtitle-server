package org.xinyo.subtitle.netty.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestMapping {
    String[] value() default {};
}
