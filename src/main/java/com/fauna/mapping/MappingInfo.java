package com.fauna.mapping;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaFieldImpl;
import com.fauna.annotation.FaunaObject;
import com.fauna.interfaces.IClassDeserializer;
import com.fauna.serialization.ClassDeserializer;
import com.fauna.serialization.Serializer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MappingInfo {

    private final Type type;
    private final List<FieldInfo> fields;
    private final Map<String, FieldInfo> fieldsByName;
    private final boolean shouldEscapeObject;
    private final IClassDeserializer deserializer;

    public MappingInfo(MappingContext ctx, Type ty) {
        ctx.add(ty, this);
        this.type = ty;

        Class<?> clazz = getClassFromType(type);
        boolean hasAttributes = clazz.isAnnotationPresent(FaunaObject.class);

        List<FieldInfo> fieldsList = new ArrayList<>();
        Map<String, FieldInfo> byNameMap = new HashMap<>();

        for (Field field : ((Class<?>) ty).getDeclaredFields()) {
            FaunaFieldImpl attr;

            if (hasAttributes) {
                if (field.getAnnotation(FaunaField.class) != null) {
                    attr = new FaunaFieldImpl(field,
                        field.getAnnotation(FaunaField.class));
                } else {
                    continue;
                }
            } else {
                attr = new FaunaFieldImpl(field, null);
            }

            FieldInfo info = new FieldInfo(ctx, attr, field);

            if (byNameMap.containsKey(info.getName())) {
                throw new IllegalArgumentException(
                    "Duplicate field name " + info.getName() + " in " + ty);
            }

            fieldsList.add(info);
            byNameMap.put(info.getName(), info);
        }

        this.shouldEscapeObject = Serializer.TAGS.stream().anyMatch(byNameMap.keySet()::contains);
        this.fields = List.copyOf(fieldsList);
        this.fieldsByName = Map.copyOf(byNameMap);
        this.deserializer = new ClassDeserializer(this);

    }

    public Type getType() {
        return type;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public Map<String, FieldInfo> getFieldsByName() {
        return fieldsByName;
    }

    public boolean shouldEscapeObject() {
        return shouldEscapeObject;
    }

    public IClassDeserializer getDeserializer() {
        return deserializer;
    }

    private Class<?> getClassFromType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type.getTypeName());
        }
    }
}