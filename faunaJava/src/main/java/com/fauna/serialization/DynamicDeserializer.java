package com.fauna.serialization;

import com.fauna.exception.SerializationException;

/**
 * Deserializer that dynamically deserializes objects of various types.
 *
 * @param <T> The type of object being deserialized.
 */
public class DynamicDeserializer<T> extends BaseDeserializer<T> {

    private static DynamicDeserializer<?> instance = new DynamicDeserializer<>();

    /**
     * Returns the singleton instance of DynamicDeserializer.
     *
     * @param <T> The type of object being deserialized.
     * @return The singleton instance of DynamicDeserializer.
     */
    public static <T> DynamicDeserializer<T> getInstance() {
        return (DynamicDeserializer<T>) instance;
    }

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private DynamicDeserializer() {

    }

    /**
     * Deserializes the value from the FaunaParser.
     *
     * @param context The serialization context.
     * @param reader  The FaunaParser instance to read from.
     * @return The deserialized value.
     */
    public T deserialize(SerializationContext context, FaunaParser reader) {
        Object value = null;
        switch (reader.getCurrentTokenType()) {
            case START_OBJECT:
            case START_ARRAY:
            case END_PAGE:
            case START_REF:
            case START_DOCUMENT:
            case MODULE:
                break;
            case INT:
                value = reader.getValueAsInt();
                break;
            case STRING:
                value = reader.getValueAsString();
                break;
            case DATE:
                value = reader.getValueAsLocalDate();
                break;
            case TIME:
                value = reader.getValueAsTime();
                break;
            case NULL:
                value = null;
                break;
            case DOUBLE:
                value = reader.getValueAsDouble();
                break;
            case LONG:
                value = reader.getValueAsLong();
                break;
            case TRUE:
            case FALSE:
                value = reader.getValueAsBoolean();
                break;
            default:
                throw new SerializationException(
                    "Unexpected token while deserializing: " + reader.getCurrentTokenType());
        }

        return (T) value;
    }

}
