package com.fauna.codec.codecs;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaFieldImpl;
import com.fauna.annotation.FaunaIgnore;
import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.mapping.FieldInfo;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassCodec<T> extends BaseCodec<T> {
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private final Class<T> type;
    private final List<FieldInfo> fields;
    private final Map<String, FieldInfo> fieldsByName;
    private final boolean shouldEscapeObject;

    public ClassCodec(Class<T> ty, CodecProvider provider) {
        this.type = ty;

        List<FieldInfo> fieldsList = new ArrayList<>();
        Map<String, FieldInfo> byNameMap = new HashMap<>();

        for (Field field : ((Class<?>) ty).getDeclaredFields()) {
            if (field.getAnnotation(FaunaIgnore.class) != null) {
                continue;
            }

            var attr = new FaunaFieldImpl(field, field.getAnnotation(FaunaField.class));

            if (byNameMap.containsKey(attr.name())) {
                throw new IllegalArgumentException(
                        "Duplicate field name " + attr.name() + " in " + ty);
            }

            var ta = attr.typeArgument() != void.class ? attr.typeArgument() : null;
            // Don't init the codec here because of potential circular references; instead use a provider.
            FieldInfo info = new FieldInfo(field, attr.name(), ta, provider);
            fieldsList.add(info);
            byNameMap.put(info.getName(), info);
        }

        this.shouldEscapeObject = TAGS.stream().anyMatch(byNameMap.keySet()::contains);
        this.fields = List.copyOf(fieldsList);
        this.fieldsByName = Map.copyOf(byNameMap);
    }

    @Override
    public T decode(UTF8FaunaParser parser) {
        try {
            FaunaTokenType endToken = parser.getCurrentTokenType().getEndToken();
            Object instance = createInstance();
            setFields(instance, parser, endToken);
            @SuppressWarnings("unchecked")
            T typed = (T) instance;
            return typed;
        } catch (IllegalAccessException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                 NoSuchMethodException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, T obj) throws IOException {
        if (shouldEscapeObject) {
            gen.writeStartEscapedObject();
        } else {
            gen.writeStartObject();
        }
        for (FieldInfo fi : fields) {
            if (!fi.getName().startsWith("this$")) {
                var fieldName = fi.getName();
                try {
                    fi.getProperty().setAccessible(true);
                    @SuppressWarnings("unchecked")
                    T value = obj != null ? (T) fi.getProperty().get(obj) : null;

                    if (value == null) {
                        continue;
                    }

                    gen.writeFieldName(fieldName);
                    @SuppressWarnings("unchecked")
                    Codec<T> codec = fi.getCodec();
                    codec.encode(gen, value);
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
    public Class<T> getCodecClass() {
        return this.type;
    }

    private Object createInstance() throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        Class<?> clazz = Class.forName(type.getTypeName());
        Constructor<?> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    private void setFields(Object instance, UTF8FaunaParser parser,
                           FaunaTokenType endToken) throws IOException, IllegalAccessException {

        InternalDocument.Builder builder = new InternalDocument.Builder();

        while (parser.read() && parser.getCurrentTokenType() != endToken) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
            }

            String fieldName = parser.getValueAsString();
            parser.read();

            if (endToken == FaunaTokenType.END_REF) {
                builder = builder.withRefField(fieldName, parser);
            }

            if (fieldName.equals(ID_FIELD)) {
                trySetId(fieldName, instance, parser);
            } else if (fieldName.equals(NAME_FIELD)) {
                trySetName(fieldName, instance, parser);
            } else {
                trySetField(fieldName, instance, parser);
            }
        }

        // Throws if it does not exist, otherwise no-op.
        builder.build();
    }

    private void trySetId(String fieldName, Object instance, UTF8FaunaParser parser) throws IllegalAccessException {
        if (parser.getCurrentTokenType() != FaunaTokenType.STRING) return;

        FieldInfo field = fieldsByName.get(fieldName);
        if (field != null) {

            String id = parser.getValueAsString();
            field.getProperty().setAccessible(true);

            if (field.getType() == Long.class) {
                field.getProperty().set(instance, Long.parseLong(id));
            } else if (field.getType() == String.class) {
                field.getProperty().set(instance, id);
            }
        }
    }

    private void trySetName(String fieldName,Object instance, UTF8FaunaParser parser) throws IllegalAccessException {
        if (parser.getCurrentTokenType() != FaunaTokenType.STRING) return;

        FieldInfo field = fieldsByName.get(fieldName);
        if (field != null) {
            String name = parser.getValueAsString();
            field.getProperty().setAccessible(true);
            if (field.getType() == String.class) {
                field.getProperty().set(instance, name);
            }
        }
    }

    private void trySetField(String fieldName, Object instance, UTF8FaunaParser parser) throws IOException, IllegalAccessException {
        FieldInfo field = fieldsByName.get(fieldName);
        if (field == null) {
            parser.skip();
        } else {
            field.getProperty().setAccessible(true);
            field.getProperty().set(instance, field.getCodec().decode(parser));
        }
    }
}
