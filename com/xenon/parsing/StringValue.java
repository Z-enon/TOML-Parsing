package com.xenon.parsing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the possible values for a variable as a string, or characters.
 * @author Zenon
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE_PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
public @interface StringValue {
    String value();
}
