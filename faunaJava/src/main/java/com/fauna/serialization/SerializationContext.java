package com.fauna.serialization;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents the context for serialization and deserialization operations within Fauna.
 */
public class SerializationContext {

    private final Map<TypeToken<?>, Map<String, FieldAttribute>> registry = new HashMap<>();

    /**
     * Retrieves the mapping of property names to their corresponding {@link FieldAttribute} for a
     * given Java type.
     *
     * @param typeToken The type for which the field map is requested.
     * @return A map where keys are property names and values are the corresponding
     * {@link FieldAttribute} instances.
     */
    public <T> Map<String, FieldAttribute> getFieldMap(TypeToken<T> typeToken) {
        if (registry.containsKey(typeToken)) {
            return registry.get(typeToken);
        }

        Field[] fields = typeToken.getRawType().getDeclaredFields();
        Map<String, FieldAttribute> newFieldMap = new HashMap<>();
        boolean hasAttributes = typeToken.getRawType()
            .isAnnotationPresent(FaunaObjectAttribute.class);

        for (Field field : fields) {
            FieldAttribute attribute;
            if (hasAttributes) {
                FieldAttribute annotation = field.getAnnotation(FieldAttribute.class);
                if (annotation != null) {
                    attribute = new FieldAttributeImpl(field, annotation);
                } else {
                    continue;
                }
            } else {
                attribute = new FieldAttributeImpl(field, null);
            }
            newFieldMap.put(attribute.name(), attribute);
        }

        registry.put(typeToken, newFieldMap);
        return newFieldMap;
    }

}