package com.fauna.serialization;

import com.fauna.exception.SerializationException;

public class DynamicDeserializer<T> extends BaseDeserializer<T> {

    private static DynamicDeserializer<?> instance = new DynamicDeserializer<>();

    public static <T> DynamicDeserializer<T> getInstance(Class<T> type) {
        return (DynamicDeserializer<T>) instance;
    }

    private DynamicDeserializer() {

    }

    public T deserialize(SerializationContext context, FaunaParser reader) {
        Object value = null;
        switch (reader.getCurrentTokenType()) {
            case START_OBJECT:
            case START_ARRAY:
            case END_PAGE:
            case START_REF:
            case START_DOCUMENT:
            case LONG:
            case DOUBLE:
            case DATE:
            case TIME:
            case TRUE:
            case FALSE:
            case MODULE:
                break;
            case INT:
                value = reader.getValueAsInt();
                break;
            case STRING:
                value = reader.getValueAsString();
                break;
            case NULL:
                value = null;
                break;
            default:
                throw new SerializationException(
                    "Unexpected token while deserializing: " + reader.getCurrentTokenType());
        }

        return (T) value;
    }

}
