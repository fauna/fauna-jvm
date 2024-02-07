package com.fauna.serialization;

import com.fauna.exception.SerializationException;

public class DynamicDeserializer extends BaseDeserializer {

    public static DynamicDeserializer instance = new DynamicDeserializer();

    public static DynamicDeserializer getInstance() {
        if (instance == null) {
            synchronized (DynamicDeserializer.class) {
                if (instance == null) {
                    instance = new DynamicDeserializer();
                }
            }
        }
        return instance;
    }

    private DynamicDeserializer() {

    }


    public Object deserialize(SerializationContext context, FaunaParser reader) {
        Object value = null;
        switch (reader.getCurrentTokenType()) {
            case START_OBJECT:
            case START_ARRAY:
            case END_PAGE:
            case START_REF:
            case START_DOCUMENT:
            case STRING:
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
            case NULL:
                value = null;
                break;
            default:
                throw new SerializationException(
                    "Unexpected token while deserializing: " + reader.getCurrentTokenType());
        }

        return value;
    }

    @Override
    public Object deserializeGeneric(SerializationContext context, FaunaParser parser) {
        return null;
    }
}
