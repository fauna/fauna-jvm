package com.fauna.serialization;

import com.fauna.common.enums.FaunaType;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;


public class SerializationContext {

    private final Map<Class<?>, Map<String, FieldInfo>> registry = new HashMap<>();

    public <T> IDeserializer<T> getDeserializer(Class<T> clazz) {
        return Deserializer.generate(this, clazz);
    }

    public Map<String, FieldInfo> getFieldMap(Class<?> clazz) {
        if (registry.containsKey(clazz)) {
            return registry.get(clazz);
        }

        Map<String, FieldInfo> fieldMap = new HashMap<>();
        PropertyDescriptor[] props = getPropertyDescriptors(clazz);
        boolean hasAttributes = clazz.getAnnotation(FaunaObjectAttribute.class) != null;

        for (PropertyDescriptor prop : props) {
            FieldAttribute attr = prop.getReadMethod().getAnnotation(FieldAttribute.class);
            if (attr == null) {
                continue;
            }

            String name = attr.name();
            FaunaType type = attr.type();
            fieldMap.put(name, new FieldInfo(name, type));
        }

        registry.put(clazz, fieldMap);
        return fieldMap;
    }

    private PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            return beanInfo.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new RuntimeException(
                "Failed to get property descriptors for class: " + clazz.getName(), e);
        }
    }
}