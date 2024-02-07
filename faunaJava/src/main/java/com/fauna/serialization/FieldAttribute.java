package com.fauna.serialization;

import com.fauna.common.enums.FaunaType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldAttribute {

    String name() default "";

    FaunaType type() default FaunaType.UNKNOWN;
}