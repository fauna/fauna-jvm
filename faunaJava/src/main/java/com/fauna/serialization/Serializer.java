package com.fauna.serialization;

import com.fauna.common.enums.FaunaType;
import com.fauna.common.types.Module;
import com.fauna.exception.SerializationException;
import com.fauna.mapping.MappingContext;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Serializer {

    public static final Set<String> TAGS = new HashSet<>(
        Arrays.asList("@int", "@long", "@double", "@date", "@time", "@mod", "@ref", "@doc", "@set",
            "@object"));

    public static void serialize(MappingContext context, FaunaGenerator writer, Object obj)
        throws IOException {
        serialize(context, writer, obj, null);
    }

    public static void serialize(MappingContext context, FaunaGenerator writer, Object obj,
        FaunaType typeHint) throws IOException {
        if (typeHint != null) {
            if (obj == null) {
                throw new NullPointerException("obj");
            }

            switch (typeHint) {
                case INT:
                    if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer) {
                        int intValue = ((Number) obj).intValue();
                        writer.writeIntValue(intValue);
                    } else {
                        throw new SerializationException(
                            "Unsupported Int conversion. Provided value must be a byte, short, or int.");
                    }
                    break;
                case LONG:
                    if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer
                        || obj instanceof Long) {
                        long longValue = ((Number) obj).longValue();
                        writer.writeLongValue(longValue);
                    } else {
                        throw new SerializationException(
                            "Unsupported Long conversion. Provided value must be a byte, short, int, or long.");
                    }
                    break;
                case DOUBLE:
                    if (obj instanceof Float || obj instanceof Double || obj instanceof Short
                        || obj instanceof Integer || obj instanceof Long) {
                        double doubleValue = ((Number) obj).doubleValue();
                        writer.writeDoubleValue(doubleValue);
                    } else {
                        throw new SerializationException(
                            "Unsupported Double conversion. Provided value must be a short, int, long, float, or double.");
                    }
                    break;
                case STRING:
                    writer.writeStringValue(obj != null ? obj.toString() : "");
                    break;
                case DATE:
                    if (obj instanceof LocalDate) {
                        writer.writeDateValue((LocalDate) obj);
                    } else {
                        throw new SerializationException(
                            "Unsupported Date conversion. Provided value must be a Date.");
                    }
                    break;
                case TIME:
                    if (obj instanceof Instant) {
                        writer.writeTimeValue((Instant) obj);
                    } else {
                        throw new SerializationException(
                            "Unsupported Time conversion. Provided value must be a Date.");
                    }
                    break;
                case BOOLEAN:
                    if (obj instanceof Boolean) {
                        writer.writeBooleanValue((Boolean) obj);
                    } else {
                        throw new SerializationException(
                            "Unsupported Boolean conversion. Provided value must be a Boolean.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown typeHint: " + typeHint);
            }
        } else {
            if (obj == null) {
                writer.writeNullValue();
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
            } else if (obj instanceof String) {
                writer.writeStringValue((String) obj);
            } else if (obj instanceof Module) {
                writer.writeModuleValue((Module) obj);
            } else if (obj instanceof LocalDate) {
                writer.writeDateValue((LocalDate) obj);
            } else if (obj instanceof Instant) {
                writer.writeTimeValue((Instant) obj);
            } else {
                throw new SerializationException("Not implemented. Class: " + obj.getClass());
                //serializeObjectInternal(writer, obj, context);
            }
        }
    }
}
