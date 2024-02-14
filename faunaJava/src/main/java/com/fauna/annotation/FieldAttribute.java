package com.fauna.annotation;

import com.fauna.common.enums.FaunaType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attribute used to specify properties of a field in a Fauna object.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldAttribute {

    /**
     * Gets or sets the name of the field represented by this attribute.
     */
    String fieldName() default "";

    /**
     * Gets or sets the name of the field.
     */
    String name() default "";

    /**
     * Gets or sets the type of the field.
     */
    FaunaType type() default FaunaType.UNKNOWN;

}