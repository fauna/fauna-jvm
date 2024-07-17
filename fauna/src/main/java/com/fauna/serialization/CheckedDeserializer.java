package com.fauna.serialization;

import com.fauna.exception.ClientException;

import java.io.IOException;

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
     * @param reader The FaunaParser instance to read from.
     * @return The deserialized value.
     * @throws ClientException If the deserialized value does not match the specified type.
     */
    public T doDeserialize(UTF8FaunaParser reader) throws IOException {
        Object tokenType = reader.getCurrentTokenType();
        Object obj = DynamicDeserializer.getInstance().checkedDeserialize(reader, type);

        if (type.isInstance(obj)) {
            return type.cast(obj);
        } else {
            throw new ClientException(
                "Unexpected token while deserializing: " + tokenType);
        }
    }
}