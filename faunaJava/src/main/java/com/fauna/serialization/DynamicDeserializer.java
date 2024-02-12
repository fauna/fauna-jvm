package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;
import com.fauna.common.types.Document;
import com.fauna.common.types.Module;
import com.fauna.common.types.NamedDocument;
import com.fauna.exception.SerializationException;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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
    public T deserialize(SerializationContext context, FaunaParser reader) throws IOException {
        Object value = null;
        switch (reader.getCurrentTokenType()) {
            case START_OBJECT:
            case START_ARRAY:
            case END_PAGE:
            case START_REF:
                break;
            case START_DOCUMENT:
                value = deserializeDocument(context, reader);
                break;
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

    private Object deserializeDocument(SerializationContext context, FaunaParser reader)
        throws IOException {
        Map<String, Object> data = new HashMap<>();
        String id = null;
        String name = null;
        Instant ts = null;
        Module coll = null;

        while (reader.read() && reader.getCurrentTokenType() != FaunaTokenType.END_DOCUMENT) {
            if (reader.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new SerializationException(
                    "Unexpected token while deserializing into Document: "
                        + reader.getCurrentTokenType());
            }

            String fieldName = reader.getValueAsString();
            reader.read();
            switch (fieldName) {
                case "id":
                    id = reader.getValueAsString();
                    break;
                case "name":
                    name = reader.getValueAsString();
                    break;
                case "ts":
                    ts = reader.getValueAsTime();
                    break;
                case "coll":
                    coll = reader.getValueAsModule();
                    break;
                default:
                    data.put(fieldName,
                        DynamicDeserializer.getInstance().deserialize(context, reader));
                    break;
            }
        }

        if (id != null && coll != null && ts != null) {
            if (name != null) {
                data.put("name", name);
            }
            return new Document(id, coll, ts, data);
        }

        if (name != null && coll != null && ts != null) {
            return new NamedDocument(name, coll, ts, data);
        }

        if (id != null) {
            data.put("id", id);
        }
        if (name != null) {
            data.put("name", name);
        }
        if (coll != null) {
            data.put("coll", coll);
        }
        if (ts != null) {
            data.put("ts", ts);
        }
        return data;
    }

}
