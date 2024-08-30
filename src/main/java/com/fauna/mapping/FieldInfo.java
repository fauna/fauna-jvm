package com.fauna.mapping;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;

import java.lang.reflect.Field;

public final class FieldInfo {

    private final String name;
    private final Field property;
    private final Class<?> typeArg;
    private final CodecProvider provider;
    private Codec<?> codec;

    public FieldInfo(Field prop, String name, Class<?> typeArg, CodecProvider provider) {
        this.name = name;
        this.property = prop;
        this.typeArg = typeArg;
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public Field getProperty() {
        return property;
    }

    public Class<?> getType() {
        return property.getType();
    }

    public Codec getCodec() {
        if (codec != null) return codec;

        synchronized (this) {
            // check again in case it was set by another thread
            if (codec != null) return codec;

            codec = provider.get(property.getType(), typeArg);
        }

        return codec;
    }
}
