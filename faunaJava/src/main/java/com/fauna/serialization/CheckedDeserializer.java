package com.fauna.serialization;

import com.fauna.exception.SerializationException;

/**
 * Deserializer that checks the deserialized value against a specified type.
 *
 * @param <T> The type of object being deserialized.
 */
public class CheckedDeserializer<T> extends BaseDeserializer<T> {

    private final Class<T> type;

    /**
     * Constructs a new CheckedDeserializer with the specified type.
     *
     * @param type The type to check against during deserialization.
     */
    public CheckedDeserializer(Class<T> type) {
        this.type = type;
    }

    /**
     * Deserializes the value from the FaunaParser and checks if it matches the specified type.
     *
     * @param context The serialization context.
     * @param reader  The FaunaParser instance to read from.
     * @return The deserialized value.
     * @throws SerializationException If the deserialized value does not match the specified type.
     */
    public T deserialize(SerializationContext context, FaunaParser reader) {
        Object tokenType = reader.getCurrentTokenType();
        Object obj = DynamicDeserializer.getInstance().deserialize(context, reader);

        if (type.isInstance(obj)) {
            return type.cast(obj);
        } else {
            throw new SerializationException(
                "Unexpected token while deserializing: " + tokenType);
        }
    }
}