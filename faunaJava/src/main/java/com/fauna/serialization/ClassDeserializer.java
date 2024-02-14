package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;
import com.fauna.exception.SerializationException;
import java.lang.reflect.Field;
import java.util.Map;

public class ClassDeserializer<T> extends BaseDeserializer<T> {

    private final Map<String, FieldAttribute> _fieldMap;
    private final Class<T> _targetType;

    public ClassDeserializer(Map<String, FieldAttribute> fieldMap, Class<T> targetType) {
        _fieldMap = fieldMap;
        _targetType = targetType;
    }

    @Override
    public T deserialize(SerializationContext context, FaunaParser reader) {
        FaunaTokenType endToken;
        switch (reader.getCurrentTokenType()) {
            case START_DOCUMENT:
                endToken = FaunaTokenType.END_DOCUMENT;
                break;
            case START_OBJECT:
                endToken = FaunaTokenType.END_OBJECT;
                break;
            default:
                throw new SerializationException(
                    "Unexpected token while deserializing into Class: "
                        + reader.getCurrentTokenType());
        }

        try {
            T instance = _targetType.getDeclaredConstructor().newInstance();

            while (reader.read() && reader.getCurrentTokenType() != endToken) {
                if (reader.getCurrentTokenType() == FaunaTokenType.FIELD_NAME) {
                    String fieldName = reader.getValueAsString();
                    reader.read();

                    if (_fieldMap.containsKey(fieldName)) {
                        FieldAttribute fieldAttribute = _fieldMap.get(fieldName);
                        Field field = _targetType.getDeclaredField(fieldAttribute.fieldName());
                        field.setAccessible(true);
                        field.set(instance,
                            DynamicDeserializer.getInstance().deserialize(context, reader));
                    } else {
                        reader.skip();
                    }
                } else {
                    throw new SerializationException(
                        "Unexpected token while deserializing into Class: "
                            + reader.getCurrentTokenType());
                }
            }

            return instance;
        } catch (Exception e) {
            throw new SerializationException("Error deserializing class " + _targetType.getName(),
                e);
        }
    }
}