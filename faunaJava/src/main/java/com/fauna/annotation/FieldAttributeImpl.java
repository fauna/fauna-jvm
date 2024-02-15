package com.fauna.annotation;

import com.fauna.common.enums.FaunaType;
import java.lang.reflect.Field;

public class FieldAttributeImpl implements FieldAttribute {

    private final Field field;
    private final FieldAttribute annotation;

    public FieldAttributeImpl(Field field, FieldAttribute annotation) {
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
        return (annotation != null) ? annotation.type() : FaunaType.UNKNOWN;
    }

    @Override
    public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return FieldAttribute.class;
    }
}
