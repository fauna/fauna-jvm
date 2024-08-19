package com.fauna.mapping;

import com.fauna.annotation.FaunaFieldImpl;
import com.fauna.codec.Codec;
import com.fauna.interfaces.IDeserializer;
import com.fauna.serialization.Deserializer;
import com.fauna.serialization.NullableDeserializer;

import java.lang.reflect.Field;

public final class FieldInfo {

    private final String name;
    private final Field property;
    private final Class<?> type;
    private Codec<?> codec;

    // TODO: remove this when we flip to using codecs
    private final MappingContext ctx;
    // TODO: remove this when we flip to using codecs
    private IDeserializer<?> deserializer;
    // TODO: remove this when we flip to using codecs
    private boolean isNullable;

    public FieldInfo(MappingContext ctx, FaunaFieldImpl attr, Field prop) {
        this.ctx = ctx;
        this.name = attr.name();
        this.property = prop;
        this.type = prop.getType();
        this.isNullable = attr.nullable();
        this.codec = null;
    }

    public FieldInfo(Field prop, String name, Codec codec) {
        this.name = name;
        this.property = prop;
        this.type = prop.getType();
        this.codec = codec;

        // TODO: remove this when we've flipped to using codecs
        this.ctx = null;
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

    public IDeserializer<?> getDeserializer() {
        synchronized (ctx) {
            if (deserializer == null) {
                deserializer = Deserializer.generate(ctx, type);
                if (isNullable) {
                    deserializer = new NullableDeserializer<>(deserializer);
                }
            }
            return deserializer;
        }
    }

    public Codec getCodec() {
        return this.codec;
    }
}
