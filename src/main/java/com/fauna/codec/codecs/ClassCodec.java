package com.fauna.codec.codecs;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaFieldImpl;
import com.fauna.codec.CodecProvider;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.exception.NullDocumentException;
import com.fauna.mapping.FieldInfo;
import com.fauna.mapping.FieldName;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;
import com.fauna.types.Module;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassCodec<T> extends BaseCodec<T> {
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String COLL_FIELD = "coll";
    private static final String EXISTS_FIELD = "exists";
    private static final String CAUSE_FIELD = "cause";


    private final Type type;
    private final List<FieldInfo> fields;
    private final Map<String, FieldInfo> fieldsByName;
    private final boolean shouldEscapeObject;

    public ClassCodec(Type ty, CodecProvider provider) {
        this.type = ty;

        List<FieldInfo> fieldsList = new ArrayList<>();
        Map<String, FieldInfo> byNameMap = new HashMap<>();

        for (Field field : ((Class<?>) ty).getDeclaredFields()) {
            if (field.getAnnotation(FaunaField.class) == null) {
                continue;
            }

            var attr = new FaunaFieldImpl(field, field.getAnnotation(FaunaField.class));

            var name = !attr.name().isEmpty() ? attr.name() : FieldName.canonical(field.getName());

            if (byNameMap.containsKey(name)) {
                throw new IllegalArgumentException(
                        "Duplicate field name " + name + " in " + ty);
            }

            var ta = attr.typeArgument() != void.class ? attr.typeArgument() : null;
            var codec = provider.get(field.getType(), ta);
            FieldInfo info = new FieldInfo(field, name, codec);
            fieldsList.add(info);
            byNameMap.put(info.getName(), info);
        }

        this.shouldEscapeObject = TAGS.stream().anyMatch(byNameMap.keySet()::contains);
        this.fields = List.copyOf(fieldsList);
        this.fieldsByName = Map.copyOf(byNameMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T decode(UTF8FaunaParser parser) {
        try {
            FaunaTokenType endToken = parser.getCurrentTokenType().getEndToken();
            Object instance = createInstance();
            setFields(instance, parser, endToken);
            return (T) instance;
        } catch (IOException exc) {
            throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void encode(UTF8FaunaGenerator gen, T obj) throws IOException {
        if (shouldEscapeObject) {
            gen.writeStartEscapedObject();
        } else {
            gen.writeStartObject();
        }
        for (FieldInfo fi : fields) {
            if (!fi.getName().startsWith("this$")) {
                gen.writeFieldName(fi.getName());
                try {
                    fi.getProperty().setAccessible(true);
                    var value = fi.getProperty().get(obj);
                    fi.getCodec().encode(gen, value);
                } catch (IllegalAccessException e) {
                    throw new ClientException("Error accessing field: " + fi.getName(),
                            e);
                }
            }
        }
        if (shouldEscapeObject) {
            gen.writeEndEscapedObject();
        } else {
            gen.writeEndObject();
        }
    }

    @Override
    public Class<?> getCodecClass() {
        return null;
    }

    private Object createInstance() {
        try {
            Class<?> clazz = Class.forName(type.getTypeName());
            Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setFields(Object instance, UTF8FaunaParser parser,
                           FaunaTokenType endToken) throws IOException, IllegalAccessException {
        String id = null;
        String name = null;
        Module coll = null;
        boolean exists = true;
        String cause = null;

        while (parser.read() && parser.getCurrentTokenType() != endToken) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
            }

            String fieldName = parser.getValueAsString();
            parser.read();

            if (fieldName.equals(ID_FIELD)
                    && parser.getCurrentTokenType() == FaunaTokenType.STRING) {
                id = parser.getValueAsString();
                trySetId(instance, parser.getValueAsString());
            } else if (fieldName.equals(NAME_FIELD)
                    && parser.getCurrentTokenType() == FaunaTokenType.STRING) {
                name = parser.getValueAsString();
                trySetName(instance, parser.getValueAsString());
            } else {
                if (fieldName.equals(COLL_FIELD) && parser.getCurrentTokenType() == FaunaTokenType.MODULE) {
                    coll = parser.getValueAsModule();
                }

                if (fieldName.equals(EXISTS_FIELD) && parser.getCurrentTokenType() == FaunaTokenType.FALSE) {
                    exists = parser.getValueAsBoolean();
                }

                if (fieldName.equals(CAUSE_FIELD) && parser.getCurrentTokenType() == FaunaTokenType.STRING) {
                    cause = parser.getValueAsString();
                }

                FieldInfo field = fieldsByName.get(fieldName);
                if (field != null) {
                    field.getProperty().setAccessible(true);
                    try {
                        field.getProperty()
                                .set(instance, field.getCodec().decode(parser));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    parser.skip();
                }
            }
        }

        if (endToken == FaunaTokenType.END_REF && !exists) {
            throw new NullDocumentException(id != null ? id : name, coll, cause);
        }
    }

    private void trySetId(Object instance, String id) throws IllegalAccessException {
        FieldInfo field = fieldsByName.get(ID_FIELD);
        if (field != null) {
        field.getProperty().setAccessible(true);
            if (field.getType() == Long.class) {
                field.getProperty().set(instance, Long.parseLong(id));
            } else if (field.getType() == String.class) {
                field.getProperty().set(instance, id);
            } else {
                throw new ClientException(String.format("Unsupported type for Id field: %s", field.getType()));
            }
        }
    }

    private void trySetName(Object instance, String name) throws IllegalAccessException {
        FieldInfo field = fieldsByName.get(NAME_FIELD);
        if (field != null) {
            field.getProperty().setAccessible(true);
            if (field.getType() == String.class) {
                field.getProperty().set(instance, name);
            } else {
                throw new ClientException(String.format("Unsupported type for Name field: %s", field.getType()));
            }
        }
    }
}
