package com.fauna.mapping;

import com.fauna.codec.Codec;

import java.lang.reflect.Field;

public final class FieldInfo {

    private final String name;
    private final Field property;
    private final Class<?> type;
    private Codec<?> codec;

    public FieldInfo(Field prop, String name, Codec codec) {
        this.name = name;
        this.property = prop;
        this.type = prop.getType();
        this.codec = codec;
    }

    public String getName() {
        return name;
    }

    public Field getProperty() {
        return property;
    }

    public Class<?> getType() {
        return type;
    }

    public Codec getCodec() {
        return this.codec;
    }
}
