package com.fauna.codec;

import com.fauna.enums.FaunaTokenType;
import com.fauna.mapping.FieldInfo;
import com.fauna.mapping.MappingContext;
import com.fauna.query.builder.LiteralFragment;
import com.fauna.serialization.Serializer;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;
import com.fauna.types.Document;
import com.fauna.types.DocumentRef;
import com.fauna.types.Module;
import com.fauna.types.NamedDocument;
import com.fauna.types.NamedDocumentRef;
import com.fauna.types.NullDocumentRef;
import com.fauna.types.NullNamedDocumentRef;
import com.fauna.exception.ClientException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;


/**
 * Codec that dynamically deserializes objects of various types.
 *
 * @param <T> The type of object being deserialized.
 */
public class DynamicCodec<T> implements Codec {

    private static DynamicCodec<?> instance = new DynamicCodec<>();

    public static final Set<String> TAGS = new HashSet<>(
            Arrays.asList("@int", "@long", "@double", "@date", "@time", "@mod", "@ref", "@doc", "@set",
                    "@object"));

    /**
     * Returns the singleton instance of DynamicCodec.
     *
     * @param <T> The type of object being deserialized.
     * @return The singleton instance of DynamicCodec.
     */
    public static <T> DynamicCodec<T> getInstance() {
        return (DynamicCodec<T>) instance;
    }

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private DynamicCodec() {
    }

    public static String encode(Object obj) throws IOException {
        UTF8FaunaGenerator gen = new UTF8FaunaGenerator();
        getInstance().encode(gen, obj);
        return gen.serialize();
    }

    @Override
    public T decode(UTF8FaunaParser reader) throws IOException {
        return checkedCodec(reader, null);
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, Object obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else if (obj instanceof LiteralFragment) {
            gen.writeStringValue( ((LiteralFragment) obj).getValue());
        } else if (obj instanceof Byte) {
            gen.writeIntValue((Byte) obj);
        } else if (obj instanceof Short) {
            gen.writeIntValue(((Short) obj).intValue());
        } else if (obj instanceof Integer) {
            gen.writeIntValue((Integer) obj);
        } else if (obj instanceof Long) {
            gen.writeLongValue((Long) obj);
        } else if (obj instanceof Float) {
            gen.writeDoubleValue(((Float) obj).doubleValue());
        } else if (obj instanceof Double) {
            gen.writeDoubleValue((Double) obj);
        } else if (obj instanceof Boolean) {
            gen.writeBooleanValue((Boolean) obj);
        } else if (obj instanceof Character) {
            gen.writeIntValue(((char) obj));
        } else if (obj instanceof String) {
            gen.writeStringValue((String) obj);
        } else if (obj instanceof Module) {
            gen.writeModuleValue((Module) obj);
        } else if (obj instanceof LocalDate) {
            gen.writeDateValue((LocalDate) obj);
        } else if (obj instanceof Instant) {
            gen.writeTimeValue((Instant) obj);
        } else if (obj instanceof byte[]) {
            gen.writeByteArray((byte[]) obj);
        } else {
            serializeObjectInternal(gen, obj);
        }
    }

    @Override
    public Class<Object> getCodecClass() {
        return Object.class;
    }

    /**
     * Decodes the value from the FaunaParser.
     *
     * @param reader The FaunaParser instance to read from.
     * @return The deserialized value.
     */
    public T checkedCodec(UTF8FaunaParser reader, Type type) throws IOException {
        Object value = null;
        switch (reader.getCurrentTokenType()) {
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
                            DynamicCodec.getInstance().decode(reader));
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
                            DynamicCodec.getInstance().decode(reader));
                    break;
            }
        }

        if (id != null && coll != null) {
            if (exists) {
                return new DocumentRef(id, coll);
            }
            return new NullDocumentRef(id, coll, cause);
        }

        if (name != null && coll != null) {
            if (exists) {
                return new NamedDocumentRef(name, coll);
            }
            return new NullNamedDocumentRef(name, coll, cause);
        }

        return allProps;
    }

    private void serializeObjectInternal(UTF8FaunaGenerator writer, Object obj) throws IOException {
        if (obj instanceof Map) {
            serializeMapInternal(writer, (Map<?, ?>) obj);
        } else if (obj instanceof List) {
            writer.writeStartArray();
            for (Object item : (List<?>) obj) {
                encode(writer, item);
            }
            writer.writeEndArray();
        } else if (obj instanceof Object[]) {
            writer.writeStartArray();
            for (Object item : (Object[]) obj) {
                encode(writer, item);
            }
            writer.writeEndArray();
        } else {
            serializeClassInternal(writer, obj, new MappingContext());
        }
    }

    private <V> void serializeMapInternal(UTF8FaunaGenerator writer, Map<?,V> map) throws IOException {
        boolean shouldEscape = map.keySet().stream().anyMatch(TAGS::contains);
        if (shouldEscape) {
            writer.writeStartEscapedObject();
        } else {
            writer.writeStartObject();
        }
        for (Map.Entry<?, V> entry : map.entrySet()) {
            writer.writeFieldName(entry.getKey().toString());
            encode(writer, entry.getValue());
        }
        if (shouldEscape) {
            writer.writeEndEscapedObject();
        } else {
            writer.writeEndObject();
        }
    }

    private void serializeClassInternal(UTF8FaunaGenerator writer, Object obj,
                                               MappingContext context) throws IOException {
        Class<?> clazz = obj.getClass();
        List<FieldInfo> fieldInfoList = context.getInfo(clazz).getFields();
        boolean shouldEscape = fieldInfoList.stream().map(FieldInfo::getName)
                .anyMatch(TAGS::contains);

        if (shouldEscape) {
            writer.writeStartEscapedObject();
        } else {
            writer.writeStartObject();
        }
        for (FieldInfo field : fieldInfoList) {
            if (shouldSerializeField(field)) {
                writer.writeFieldName(field.getName());
                try {
                    field.getProperty().setAccessible(true);
                    Object value = field.getProperty().get(obj);
                    encode(writer, value);
                } catch (IllegalAccessException e) {
                    throw new ClientException("Error accessing field: " + field.getName(),
                            e);
                }
            }
        }
        if (shouldEscape) {
            writer.writeEndEscapedObject();
        } else {
            writer.writeEndObject();
        }
    }

    private static boolean shouldSerializeField(FieldInfo field) {
        // Exclude synthetic fields
        return !field.getName().startsWith("this$");
    }
}
