package com.fauna.mapping;

import com.fauna.annotation.FieldAttributeImpl;
import com.fauna.common.enums.FaunaType;
import com.fauna.interfaces.IDeserializer;
import com.fauna.serialization.Deserializer;
import com.fauna.serialization.NullableDeserializer;
import java.lang.reflect.Field;

public final class FieldInfo {

    private final String name;
    private final Field property;
    private final FaunaType faunaTypeHint;
    private final Class<?> type;
    private final boolean isNullable;

    private final MappingContext ctx;
    private IDeserializer<?> deserializer;

    public FieldInfo(MappingContext ctx, FieldAttributeImpl attr, Field prop) {
        this.ctx = ctx;
        this.name =
            attr != null && attr.name() != null ? attr.name() : FieldName.canonical(prop.getName());
        this.faunaTypeHint = attr != null ? attr.type() : null;
        this.property = prop;
        this.type = prop.getType();
        this.isNullable = attr != null ? attr.nullable() : false;
    }

    public String getName() {
        return name;
    }

    public Field getProperty() {
        return property;
    }

    public FaunaType getFaunaTypeHint() {
        return faunaTypeHint;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isNullable() {
        return isNullable;
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
}
