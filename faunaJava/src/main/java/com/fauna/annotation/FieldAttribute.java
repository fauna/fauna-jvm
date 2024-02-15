package com.fauna.annotation;

import com.fauna.common.enums.FaunaType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldAttribute {

    String name() default "";

    boolean nullable() default false;

    FaunaType type() default FaunaType.STRING;
}