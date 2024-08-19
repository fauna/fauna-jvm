package com.fauna.serialization;

import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.types.Module;
import com.fauna.exception.ClientException;
import com.fauna.mapping.FieldInfo;
import com.fauna.mapping.MappingContext;
import com.fauna.query.builder.LiteralFragment;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public class Serializer {

    private static final MappingContext context = new MappingContext();

    public static final Set<String> TAGS = new HashSet<>(
        Arrays.asList("@int", "@long", "@double", "@date", "@time", "@mod", "@ref", "@doc", "@set",
            "@object"));

    public static String serialize(Object obj) throws IOException {
        UTF8FaunaGenerator gen = new UTF8FaunaGenerator();
        Serializer.serialize(gen, obj);
        return gen.serialize();
    }

    public static void serialize(UTF8FaunaGenerator writer, Object obj) throws IOException {
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

    private static void serializeObjectInternal(UTF8FaunaGenerator writer, Object obj) throws IOException {
        if (obj instanceof Map) {
            serializeMapInternal(writer, (Map<?, ?>) obj, context);
        } else if (obj instanceof List) {
            writer.writeStartArray();
            for (Object item : (List<?>) obj) {
                serialize(writer, item);
            }
            writer.writeEndArray();
        } else if (obj instanceof Object[]) {
            writer.writeStartArray();
            for (Object item : (Object[]) obj) {
                serialize(writer, item);
            }
            writer.writeEndArray();
        } else if (obj instanceof Optional) {
            var opt = ((Optional<?>) obj);
            if (opt.isEmpty()) {
                writer.writeNullValue();
            } else {
                serialize(writer, opt.get());
            }
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
            serialize(writer, entry.getValue());
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
                    serialize(writer, value);
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
