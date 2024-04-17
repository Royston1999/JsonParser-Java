package com.royston.jsonparser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotates {@link JsonConstructor} constructor parameters.
 * @param value the matching json key name for the value to be passed in as the parameter
 * @param nullable a {@code boolean} value for whether null can be passed in as the parameter if no matching key is found in the json
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface JsonParam {
    public String value();
    public boolean nullable() default false;
}
