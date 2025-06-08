package com.api.common.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Facade {
    /**
     * The name of the facade.
     * If not specified, the class name will be used as the default name.
     *
     * @return the name of the facade
     */
    @AliasFor(annotation = Component.class)
    String value() default "";
}
