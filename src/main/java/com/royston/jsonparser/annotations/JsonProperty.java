package com.royston.jsonparser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotates class fields as a value to be serialised
 * @param value the value for the key name in the json structure
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface JsonProperty {
    public String value();
    public boolean optional() default false;
}