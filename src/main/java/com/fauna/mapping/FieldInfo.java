package com.fauna.mapping;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Represents metadata for a field in a class, including its name, type, associated codec,
 * and other properties used for serialization and deserialization.
 */
public final class FieldInfo {

    private final String name;
    private final CodecProvider provider;
    private final FieldType fieldType;
    private final Class<?> clazz;
    private final Type[] genericTypeArgs;
    private final Field field;
    private Codec<?> codec;

    /**
     * Constructs a {@code FieldInfo} object with the specified field metadata.
     *
     * @param field          The field represented by this {@code FieldInfo} instance.
     * @param name           The name of the field.
     * @param clazz          The class type of the field.
     * @param genericTypeArgs An array of generic type arguments for the field, if any.
     * @param provider       The {@link CodecProvider} used to obtain a codec for this field.
     * @param fieldType      The {@link FieldType} of the field.
     */
    public FieldInfo(
            final Field field,
            final String name,
            final Class<?> clazz,
            final Type[] genericTypeArgs,
            final CodecProvider provider,
            final FieldType fieldType) {
        this.field = field;
        this.name = name;
        this.clazz = clazz;
        this.genericTypeArgs = genericTypeArgs;
        this.provider = provider;
        this.fieldType = fieldType;
    }

    /**
     * Retrieves the name of the field.
     *
     * @return A {@code String} representing the field name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the class type of the field.
     *
     * @return A {@code Class<?>} representing the field's class type.
     */
    public Class<?> getType() {
        return clazz;
    }

    /**
     * Retrieves the {@link FieldType} of this field.
     *
     * @return The {@code FieldType} associated with this field.
     */
    public FieldType getFieldType() {
        return fieldType;
    }

    /**
     * Retrieves the codec used to serialize and deserialize the field. If the codec is not already set,
     * it will be retrieved from the {@link CodecProvider} and cached.
     *
     * @return A {@code Codec} instance associated with the field type.
     */
    @SuppressWarnings("rawtypes")
    public Codec getCodec() {
        if (codec != null) {
            return codec;
        }

        synchronized (this) {
            // Double-checked locking to ensure thread-safe lazy initialization
            if (codec != null) {
                return codec;
            }
            codec = provider.get(clazz, genericTypeArgs);
        }

        return codec;
    }

    /**
     * Retrieves the {@code Field} object representing this field in the class.
     *
     * @return The {@code Field} associated with this {@code FieldInfo}.
     */
    public Field getField() {
        return field;
    }
}
