package com.fauna.mapping;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;

import java.lang.reflect.Field;

public final class FieldInfo {

    private final String name;
    private final Field field;
    private final Class<?> typeArg;
    private final CodecProvider provider;
    private final FieldType fieldType;
    private Codec<?> codec;

    public FieldInfo(Field field, String name, Class<?> typeArg, CodecProvider provider, FieldType fieldType) {
        this.name = name;
        this.field = field;
        this.typeArg = typeArg;
        this.provider = provider;
        this.fieldType = fieldType;
    }

    public String getName() {
        return name;
    }

    public Field getField() {
        return field;
    }

    public Class<?> getType() {
        return field.getType();
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public Codec getCodec() {
        if (codec != null) return codec;

        synchronized (this) {
            // check again in case it was set by another thread
            if (codec != null) return codec;

            codec = provider.get(field.getType(), typeArg);
        }

        return codec;
    }
}
