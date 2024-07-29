package com.fauna.serialization;

import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.Document;
import com.fauna.types.DocumentRef;
import com.fauna.types.Module;
import com.fauna.types.NamedDocument;
import com.fauna.types.NamedDocumentRef;
import com.fauna.exception.ClientException;

import java.io.IOException;
import java.lang.reflect.Type;
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

    private final MapDeserializer<T> _map;
    private final ListDeserializer<T> _list;
    private final PageDeserializer<T> _page;

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
        _map = new MapDeserializer<>(this);
        _list = new ListDeserializer<>(this);
        _page = new PageDeserializer<>(this);
    }

    public T doDeserialize(UTF8FaunaParser reader) throws IOException {
        return checkedDeserialize(reader, null);
    }

    /**
     * Deserializes the value from the FaunaParser.
     *
     * @param reader The FaunaParser instance to read from.
     * @return The deserialized value.
     */
    public T checkedDeserialize(UTF8FaunaParser reader, Type type) throws IOException {
        Object value = null;
        switch (reader.getCurrentTokenType()) {
            case START_OBJECT:
                value = _map.deserialize(reader);
                break;
            case START_ARRAY:
                value = _list.deserialize(reader);
                break;
            case START_PAGE:
                value = _page.deserialize(reader);
                break;
            case START_REF:
                value = deserializeRef(reader);
                break;
            case START_DOCUMENT:
                value = deserializeDocument(reader);
                break;
            case MODULE:
                value = reader.getValueAsModule();
                break;
            case INT:
                if (Byte.class.equals(type)) {
                    value = reader.getValueAsByte();
                } else if (Short.class.equals(type)) {
                    value = reader.getValueAsShort();
                } else if (Character.class.equals(type)) {
                    value = reader.getValueAsCharacter();
                } else {
                    value = reader.getValueAsInt();
                }
                break;
            case STRING:
                if (Character.class.equals(type)) {
                    value = reader.getValueAsCharacter();
                } else {
                    value = reader.getValueAsString();
                }
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
                // Fun fact: If you try to use a ternary operator (?) here, it will cast float to double?
                if (Float.class.equals(type)) {
                    value = reader.getValueAsFloat();
                } else {
                    value = reader.getValueAsDouble();
                }
                break;
            case LONG:
                value = reader.getValueAsLong();
                break;
            case TRUE:
            case FALSE:
                value = reader.getValueAsBoolean();
                break;
            default:
                throw new ClientException(
                    "Unexpected token while deserializing: " + reader.getCurrentTokenType());
        }

        return (T) value;
    }

    private Object deserializeDocument(UTF8FaunaParser reader)
        throws IOException {
        Map<String, Object> data = new HashMap<>();
        String id = null;
        String name = null;
        Instant ts = null;
        Module coll = null;

        while (reader.read() && reader.getCurrentTokenType() != FaunaTokenType.END_DOCUMENT) {
            if (reader.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(
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
                        DynamicDeserializer.getInstance().deserialize(reader));
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

    private Object deserializeRef(UTF8FaunaParser reader)
        throws IOException {
        String id = null;
        String name = null;
        Module coll = null;
        boolean exists = true;
        String cause = null;
        Map<String, Object> allProps = new HashMap<>();

        while (reader.read() && reader.getCurrentTokenType() != FaunaTokenType.END_REF) {
            if (reader.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(
                    "Unexpected token while deserializing into DocumentRef: "
                        + reader.getCurrentTokenType());
            }

            String fieldName = reader.getValueAsString();
            reader.read();
            switch (fieldName) {
                case "id":
                    id = reader.getValueAsString();
                    allProps.put("id", id);
                    break;
                case "name":
                    name = reader.getValueAsString();
                    allProps.put("name", name);
                    break;
                case "coll":
                    coll = reader.getValueAsModule();
                    allProps.put("coll", coll);
                    break;
                case "exists":
                    exists = reader.getValueAsBoolean();
                    allProps.put("exists", exists);
                    break;
                case "cause":
                    cause = reader.getValueAsString();
                    allProps.put("cause", cause);
                    break;
                default:
                    allProps.put(fieldName,
                        DynamicDeserializer.getInstance().deserialize(reader));
                    break;
            }
        }

        if (id != null && coll != null) {
            if (exists) {
                return new DocumentRef(id, coll);
            }

            throw new NullDocumentException(id, coll, cause);
        }

        if (name != null && coll != null) {
            if (exists) {
                return new NamedDocumentRef(name, coll);
            }

            throw new NullDocumentException(name, coll, cause);
        }

        return allProps;
    }

}
