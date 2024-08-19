package com.fauna.annotation;

import com.fauna.mapping.FieldName;

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
            : FieldName.canonical(field.getName());
    }

    @Override
    public Class<?> typeArgument() {
        return annotation != null ? annotation.typeArgument() : null;
    }

    @Override
    public boolean nullable() {
        return annotation != null && annotation.nullable();
    }

    @Override
    public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return FaunaField.class;
    }
}
