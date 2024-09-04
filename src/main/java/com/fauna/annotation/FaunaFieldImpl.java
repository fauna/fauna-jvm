package com.fauna.annotation;

public class FaunaFieldImpl implements FaunaField {

    private final FaunaField annotation;

    public FaunaFieldImpl(FaunaField annotation) {
        this.annotation = annotation;
    }

    @Override
    public String name() {
        return (annotation != null && !annotation.name().isEmpty()) ? annotation.name() : null;
    }

    @Override
    public Class<?> genericTypeArgument() {
        return annotation != null ? annotation.genericTypeArgument() : null;
    }

    @Override
    public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return FaunaField.class;
    }
}
