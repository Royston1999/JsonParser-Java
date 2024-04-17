package com.royston.jsonparser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * annotates a constructor to be specifically used to deserialise json data into the class
 * @param allowNonAnnotatedParam 
 * a {@code boolean} value for whether the constructor is allowed parameters not linked with the {@link JsonParam} annotation. {@code default = false}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface JsonConstructor {
    public boolean allowNonAnnotatedParams() default false;
}
