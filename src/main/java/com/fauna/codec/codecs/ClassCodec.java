package com.fauna.codec.codecs;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaFieldImpl;
import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.exception.NullDocumentException;
import com.fauna.mapping.FieldInfo;
import com.fauna.mapping.FieldName;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    public T decode(UTF8FaunaParser parser) {
        try {
            FaunaTokenType endToken = parser.getCurrentTokenType().getEndToken();
            Object instance = createInstance();
            setFields(instance, parser, endToken);
            @SuppressWarnings("unchecked")
            T typed = (T) instance;
            return typed;
        } catch (IOException exc) {
            throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        } catch (IllegalAccessException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                 NoSuchMethodException e) {
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
                gen.writeFieldName(fi.getName());
                try {
                    fi.getProperty().setAccessible(true);
                    @SuppressWarnings("unchecked")
                    T value = (T) fi.getProperty().get(obj);
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
    public Class<?> getCodecClass() {
        return null;
    }

    private Object createInstance() throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        Class<?> clazz = Class.forName(type.getTypeName());
        Constructor<?> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    private void setFields(Object instance, UTF8FaunaParser parser,
                           FaunaTokenType endToken) throws IOException, IllegalAccessException {

        InternalRef.Builder refBuilder = new InternalRef.Builder();

        while (parser.read() && parser.getCurrentTokenType() != endToken) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
            }

            String fieldName = parser.getValueAsString();
            parser.read();

            if (endToken == FaunaTokenType.END_REF) {
                refBuilder = refBuilder.withField(fieldName, parser);
            }

            if (fieldName.equals(ID_FIELD)) {
                trySetId(fieldName, instance, parser);
            } else if (fieldName.equals(NAME_FIELD)) {
                trySetName(fieldName, instance, parser);
            } else {
                trySetField(fieldName, instance, parser);
            }
        }

        refBuilder.build().throwIfNotExists();
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
