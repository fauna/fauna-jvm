package com.fauna.mapping;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public final class FieldInfo {

    private final String name;
    private final CodecProvider provider;
    private final FieldType fieldType;
    private final Class<?> clazz;
    private final Type[] genericTypeArgs;
    private final Field field;
    private Codec<?> codec;

    public FieldInfo(Field field, String name, Class<?> clazz,
                     Type[] genericTypeArgs, CodecProvider provider,
                     FieldType fieldType) {
        this.field = field;
        this.name = name;
        this.clazz = clazz;
        this.genericTypeArgs = genericTypeArgs;
        this.provider = provider;
        this.fieldType = fieldType;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return clazz;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    @SuppressWarnings("rawtypes")
    public Codec getCodec() {
      if (codec != null) {
        return codec;
      }

        synchronized (this) {
            // check again in case it was set by another thread
          if (codec != null) {
            return codec;
          }
            codec = provider.get(clazz, genericTypeArgs);
        }

        return codec;
    }

    public Field getField() {
        return field;
    }
}
