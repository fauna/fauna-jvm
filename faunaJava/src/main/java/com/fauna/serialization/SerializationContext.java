package com.fauna.serialization;

import com.fauna.annotation.FaunaObjectAttribute;
import com.fauna.annotation.FieldAttribute;
import com.fauna.interfaces.IDeserializer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents the context for serialization and deserialization operations within Fauna.
 */
public class SerializationContext {

    private final Map<Type, Map<String, FieldAttribute>> registry = new HashMap<>();

    public <T> IDeserializer<T> getDeserializer(Type type) {
        return Deserializer.generate(this, type);
    }

    /**
     * Retrieves the mapping of property names to their corresponding {@link FieldAttribute} for a
     * given Java type.
     *
     * @param type The Type for which the field map is requested.
     * @return A map where keys are property names and values are the corresponding
     * {@link FieldAttribute} instances.
     */
    public Map<String, FieldAttribute> getFieldMap(Type type) {
        if (registry.containsKey(type)) {
            return registry.get(type);
        }

        Class<?> clazz = getClassFromType(type);
        Map<String, FieldAttribute> fieldMap = new HashMap<>();
        boolean hasAttributes = clazz.isAnnotationPresent(FaunaObjectAttribute.class);

        for (Field field : clazz.getDeclaredFields()) {
            FieldAttribute attribute;
            if (hasAttributes) {
                FieldAttribute a = field.getAnnotation(FieldAttribute.class);
                if (a == null) {
                    continue;
                }
                attribute = new FieldAttributeImpl(field, a);
            } else {
                attribute = new FieldAttributeImpl(field, null);
            }

            fieldMap.put(attribute.name(), attribute);
        }

        registry.put(type, fieldMap);
        return fieldMap;
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