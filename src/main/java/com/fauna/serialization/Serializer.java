package com.fauna.serialization;

import com.fauna.common.enums.FaunaType;
import com.fauna.common.types.Module;
import com.fauna.exception.ClientException;
import com.fauna.mapping.FieldInfo;
import com.fauna.mapping.MappingContext;
import com.fauna.query.builder.LiteralFragment;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Serializer {

    private static final MappingContext context = new MappingContext();

    public static final Set<String> TAGS = new HashSet<>(
        Arrays.asList("@int", "@long", "@double", "@date", "@time", "@mod", "@ref", "@doc", "@set",
            "@object"));

    public static String serialize(Object obj) throws IOException {
        UTF8FaunaGenerator gen = new UTF8FaunaGenerator();
        Serializer.serialize(gen, obj, null);
        return gen.serialize();
    }

    public static void serialize(UTF8FaunaGenerator writer, Object obj,
                                 FaunaType typeHint) throws IOException {
        if (typeHint != null) {
            if (obj == null) {
                throw new IllegalArgumentException("obj Param");
            }

            switch (typeHint) {
                case INT:
                    if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer) {
                        int intValue = ((Number) obj).intValue();
                        writer.writeIntValue(intValue);
                    } else {
                        throw new ClientException(
                            "Unsupported Int conversion. Provided value must be a byte, short, or int.");
                    }
                    break;
                case LONG:
                    if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer
                        || obj instanceof Long) {
                        long longValue = ((Number) obj).longValue();
                        writer.writeLongValue(longValue);
                    } else {
                        throw new ClientException(
                            "Unsupported Long conversion. Provided value must be a byte, short, int, or long.");
                    }
                    break;
                case DOUBLE:
                    if (obj instanceof Float || obj instanceof Double || obj instanceof Short
                        || obj instanceof Integer || obj instanceof Long) {
                        double doubleValue = ((Number) obj).doubleValue();
                        writer.writeDoubleValue(doubleValue);
                    } else {
                        throw new ClientException(
                            "Unsupported Double conversion. Provided value must be a short, int, long, float, or double.");
                    }
                    break;
                case STRING:
                    writer.writeStringValue(obj.toString());
                    break;
                case DATE:
                    writer.writeDateValue(toLocalDate(obj));
                    break;
                case TIME:
                    writer.writeTimeValue(toInstant(obj));
                    break;
                case BOOLEAN:
                    if (obj instanceof Boolean) {
                        writer.writeBooleanValue((Boolean) obj);
                    } else {
                        throw new ClientException(
                            "Unsupported Boolean conversion. Provided value must be a Boolean.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown typeHint: " + typeHint);
            }
        } else {
            if (obj == null) {
                writer.writeNullValue();
            } else if (obj instanceof LiteralFragment) {
                writer.writeStringValue( ((LiteralFragment) obj).getValue());
            } else if (obj instanceof Byte) {
                writer.writeIntValue((Byte) obj);
            } else if (obj instanceof Short) {
                writer.writeIntValue(((Short) obj).intValue());
            } else if (obj instanceof Integer) {
                writer.writeIntValue((Integer) obj);
            } else if (obj instanceof Long) {
                writer.writeLongValue((Long) obj);
            } else if (obj instanceof Float) {
                writer.writeDoubleValue(((Float) obj).doubleValue());
            } else if (obj instanceof Double) {
                writer.writeDoubleValue((Double) obj);
            } else if (obj instanceof Boolean) {
                writer.writeBooleanValue((Boolean) obj);
            } else if (obj instanceof Character) {
                writer.writeIntValue(((char) obj));
            } else if (obj instanceof String) {
                writer.writeStringValue((String) obj);
            } else if (obj instanceof Module) {
                writer.writeModuleValue((Module) obj);
            } else if (obj instanceof LocalDate) {
                writer.writeDateValue((LocalDate) obj);
            } else if (obj instanceof Instant) {
                writer.writeTimeValue((Instant) obj);
            } else if (obj instanceof byte[]) {
                writer.writeByteArray((byte[]) obj);
            } else {
                serializeObjectInternal(writer, obj);
            }
        }
    }

    private static void serializeObjectInternal(UTF8FaunaGenerator writer, Object obj) throws IOException {
        if (obj instanceof Map) {
            serializeMapInternal(writer, (Map<?, ?>) obj, context);
        } else if (obj instanceof List) {
            writer.writeStartArray();
            for (Object item : (List<?>) obj) {
                serialize(writer, item, null);
            }
            writer.writeEndArray();
        } else if (obj instanceof Object[]) {
            writer.writeStartArray();
            for (Object item : (Object[]) obj) {
                serialize(writer, item, null);
            }
            writer.writeEndArray();
        } else {
            serializeClassInternal(writer, obj, context);
        }
    }

    private static <T> void serializeMapInternal(UTF8FaunaGenerator writer, Map<?, T> map,
                                                 MappingContext context) throws IOException {
        boolean shouldEscape = map.keySet().stream().anyMatch(TAGS::contains);
        if (shouldEscape) {
            writer.writeStartEscapedObject();
        } else {
            writer.writeStartObject();
        }
        for (Map.Entry<?, T> entry : map.entrySet()) {
            writer.writeFieldName(entry.getKey().toString());
            serialize(writer, entry.getValue(), null);
        }
        if (shouldEscape) {
            writer.writeEndEscapedObject();
        } else {
            writer.writeEndObject();
        }
    }

    private static void serializeClassInternal(UTF8FaunaGenerator writer, Object obj,
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
                    serialize(writer, value, field.getFaunaTypeHint());
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

    public static LocalDate toLocalDate(Object obj) {
        if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        } else if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).toLocalDate();
        } else if (obj instanceof OffsetDateTime) {
            return ((OffsetDateTime) obj).toLocalDate();
        } else if (obj instanceof ZonedDateTime) {
            return ((ZonedDateTime) obj).toLocalDate();
        } else if (obj instanceof Instant) {
            return ((Instant) obj).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        throw new ClientException(
            "Unsupported Date conversion. Provided value must be a LocalDateTime, OffsetDateTime, ZonedDateTime or LocalDate but was a "
                + obj.getClass().getSimpleName());
    }

    public static Instant toInstant(Object obj) {
        if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).atZone(ZoneId.systemDefault()).toInstant();
        } else if (obj instanceof OffsetDateTime) {
            return ((OffsetDateTime) obj).toInstant();
        } else if (obj instanceof ZonedDateTime) {
            return ((ZonedDateTime) obj).toInstant();
        } else if (obj instanceof Instant) {
            return (Instant) obj;
        }
        throw new ClientException(
            "Unsupported Time conversion. Provided value must be a LocalDateTime, OffsetDateTime, ZonedDateTime but was a "
                + obj.getClass().getSimpleName());
    }
}
