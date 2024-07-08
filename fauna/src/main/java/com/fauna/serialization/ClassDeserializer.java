package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;
import com.fauna.exception.SerializationException;
import com.fauna.interfaces.IClassDeserializer;
import com.fauna.mapping.FieldInfo;
import com.fauna.mapping.MappingContext;
import com.fauna.mapping.MappingInfo;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class ClassDeserializer<T> extends BaseDeserializer<T> implements IClassDeserializer<T> {

    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";

    private final MappingInfo _info;

    public ClassDeserializer(MappingInfo info) {
        _info = info;
    }


    @Override
    public T doDeserialize(MappingContext context, FaunaParser reader) throws IOException {
        try {
            FaunaTokenType endToken = reader.getCurrentTokenType().getEndToken();
            Object instance = createInstance();
            setFields(instance, context, reader, endToken);
            return (T) instance;
        } catch (IOException exc) {
            throw unexpectedToken(reader.getCurrentTokenType());
        }
    }

    private Object createInstance() {
        try {
            Class<?> clazz = Class.forName(_info.getType().getTypeName());
            Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setFields(Object instance, MappingContext context, FaunaParser reader,
        FaunaTokenType endToken) throws IOException {
        while (reader.read() && reader.getCurrentTokenType() != endToken) {
            if (reader.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw unexpectedToken(reader.getCurrentTokenType());
            }

            String fieldName = reader.getValueAsString();
            reader.read();

            if (fieldName.equals(ID_FIELD)
                && reader.getCurrentTokenType() == FaunaTokenType.STRING) {
                trySetId(instance, reader.getValueAsString());
            } else if (fieldName.equals(NAME_FIELD)
                && reader.getCurrentTokenType() == FaunaTokenType.STRING) {
                trySetName(instance, reader.getValueAsString());
            } else {
                FieldInfo field = _info.getFieldsByName().get(fieldName);
                if (field != null) {
                    field.getProperty().setAccessible(true);
                    try {
                        field.getProperty()
                            .set(instance, field.getDeserializer().deserialize(context, reader));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    reader.skip();
                }
            }
        }
    }

    private void trySetId(Object instance, String id) {
        FieldInfo field = _info.getFieldsByName().get(ID_FIELD);
        if (field != null) {
            field.getProperty().setAccessible(true);
            try {
                if (field.getType() == Long.class) {
                    field.getProperty().set(instance, Long.parseLong(id));
                } else if (field.getType() == String.class) {
                    field.getProperty().set(instance, id);
                } else {
                    throw unexpectedToken(FaunaTokenType.STRING);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void trySetName(Object instance, String name) {
        FieldInfo field = _info.getFieldsByName().get(NAME_FIELD);
        if (field != null) {
            field.getProperty().setAccessible(true);
            try {
                if (field.getType() == String.class) {
                    field.getProperty().set(instance, name);
                } else {
                    throw unexpectedToken(FaunaTokenType.STRING);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private SerializationException unexpectedToken(FaunaTokenType tokenType) {
        return new SerializationException(
            "Unexpected token while deserializing into class " + _info.getType()
                + ": " + tokenType);
    }
}