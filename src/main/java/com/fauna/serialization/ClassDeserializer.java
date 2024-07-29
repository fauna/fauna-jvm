package com.fauna.serialization;

import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.exception.NullDocumentException;
import com.fauna.interfaces.IClassDeserializer;
import com.fauna.mapping.FieldInfo;
import com.fauna.mapping.MappingInfo;
import com.fauna.types.Module;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class ClassDeserializer<T> extends BaseDeserializer<T> implements IClassDeserializer<T> {

    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String COLL_FIELD = "coll";
    private static final String EXISTS_FIELD = "exists";
    private static final String CAUSE_FIELD = "cause";

    private final MappingInfo _info;

    public ClassDeserializer(MappingInfo info) {
        _info = info;
    }


    @Override
    public T doDeserialize(UTF8FaunaParser reader) throws IOException {
        try {
            FaunaTokenType endToken = reader.getCurrentTokenType().getEndToken();
            Object instance = createInstance();
            setFields(instance, reader, endToken);
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

    private void setFields(Object instance, UTF8FaunaParser reader,
                           FaunaTokenType endToken) throws IOException {
        String id = null;
        String name = null;
        Module coll = null;
        boolean exists = true;
        String cause = null;

        while (reader.read() && reader.getCurrentTokenType() != endToken) {
            if (reader.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw unexpectedToken(reader.getCurrentTokenType());
            }

            String fieldName = reader.getValueAsString();
            reader.read();

            if (fieldName.equals(ID_FIELD)
                && reader.getCurrentTokenType() == FaunaTokenType.STRING) {
                id = reader.getValueAsString();
                trySetId(instance, reader.getValueAsString());
            } else if (fieldName.equals(NAME_FIELD)
                && reader.getCurrentTokenType() == FaunaTokenType.STRING) {
                name = reader.getValueAsString();
                trySetName(instance, reader.getValueAsString());
            } else {
                if (fieldName.equals(COLL_FIELD) && reader.getCurrentTokenType() == FaunaTokenType.MODULE) {
                    coll = reader.getValueAsModule();
                }

                if (fieldName.equals(EXISTS_FIELD) && reader.getCurrentTokenType() == FaunaTokenType.FALSE) {
                    exists = reader.getValueAsBoolean();
                }

                if (fieldName.equals(CAUSE_FIELD) && reader.getCurrentTokenType() == FaunaTokenType.STRING) {
                    cause = reader.getValueAsString();
                }

                FieldInfo field = _info.getFieldsByName().get(fieldName);
                if (field != null) {
                    field.getProperty().setAccessible(true);
                    try {
                        field.getProperty()
                            .set(instance, field.getDeserializer().deserialize(reader));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    reader.skip();
                }
            }
        }

        if (endToken == FaunaTokenType.END_REF && !exists) {
            throw new NullDocumentException(id != null ? id : name, coll, cause);
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

    private ClientException unexpectedToken(FaunaTokenType tokenType) {
        return new ClientException(
            "Unexpected token while deserializing into class " + _info.getType()
                + ": " + tokenType);
    }
}