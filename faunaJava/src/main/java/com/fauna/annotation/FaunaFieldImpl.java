package com.fauna.annotation;

import com.fauna.common.enums.FaunaType;
import java.lang.reflect.Field;

public class FaunaFieldImpl implements FaunaField {

    private final Field field;
    private final FaunaField annotation;

    public FaunaFieldImpl(Field field, FaunaField annotation) {
        this.field = field;
        this.annotation = annotation;
    }

    @Override
    public String name() {
        return (annotation != null && !annotation.name().isEmpty()) ? annotation.name()
            : field.getName();
    }

    @Override
    public boolean nullable() {
        return annotation != null ? annotation.nullable() : false;
    }

    @Override
    public FaunaType type() {
        return (annotation != null) ? annotation.type() : null;
    }

    @Override
    public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return FaunaField.class;
    }
}
