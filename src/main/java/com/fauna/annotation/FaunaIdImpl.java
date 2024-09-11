package com.fauna.annotation;

public class FaunaIdImpl implements FaunaId {

    private final FaunaId annotation;

    public FaunaIdImpl(FaunaId annotation) {
        this.annotation = annotation;
    }

    @Override
    public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return FaunaId.class;
    }

    @Override
    public boolean isClientGenerate() {
        return annotation != null && annotation.isClientGenerate();
    }
}
