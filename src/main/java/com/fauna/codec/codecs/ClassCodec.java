package com.fauna.codec.codecs;

import com.fauna.annotation.FaunaColl;
import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaFieldImpl;
import com.fauna.annotation.FaunaId;
import com.fauna.annotation.FaunaIdImpl;
import com.fauna.annotation.FaunaIgnore;
import com.fauna.annotation.FaunaTs;
import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.mapping.FieldInfo;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.mapping.FieldName;
import com.fauna.mapping.FieldType;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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

        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }

        for (Field field : fields) {
            if (field.getAnnotation(FaunaIgnore.class) != null) {
                continue;
            }

            FieldType fieldType = getFieldType(field);

            var attr = new FaunaFieldImpl(field.getAnnotation(FaunaField.class));

            var name = attr.name() != null ? attr.name() : FieldName.canonical(field.getName());
            if (byNameMap.containsKey(name)) {
                throw new IllegalArgumentException(
                        "Duplicate field name " + name + " in " + ty);
            }

            Type type = field.getGenericType();
            FieldInfo info;

            // Don't init the codec here because of potential circular references; instead use a provider.
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType)type;
                info = new FieldInfo(field, name, (Class<?>) pType.getRawType(), pType.getActualTypeArguments(), provider, fieldType);
            } else {
                info = new FieldInfo(field, name, field.getType(), null, provider, fieldType);
            }

            fieldsList.add(info);
            byNameMap.put(info.getName(), info);
        }

        this.shouldEscapeObject = TAGS.stream().anyMatch(byNameMap.keySet()::contains);
        this.fields = List.copyOf(fieldsList);
        this.fieldsByName = Map.copyOf(byNameMap);
    }

    private FieldType getFieldType(Field field) {
        if (field.getAnnotation(FaunaId.class) != null) {
            var impl = new FaunaIdImpl(field.getAnnotation(FaunaId.class));
            if (impl.isClientGenerate()) {
                return FieldType.ClientGeneratedId;
            } else {
                return FieldType.ServerGeneratedId;
            }
        }

        if (field.getAnnotation(FaunaTs.class) != null) return FieldType.Ts;
        if (field.getAnnotation(FaunaColl.class) != null) return FieldType.Coll;
        return FieldType.Field;
    }

    @Override
    public T decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_REF:
            case START_DOCUMENT:
            case START_OBJECT:
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
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, T obj) throws CodecException {
        if (shouldEscapeObject) {
            gen.writeStartEscapedObject();
        } else {
            gen.writeStartObject();
        }
        for (FieldInfo fi : fields) {
            if (!fi.getName().startsWith("this$")) {
                var fieldType = fi.getFieldType();
                if (fieldType == FieldType.Coll || fieldType == FieldType.Ts || fieldType == FieldType.ServerGeneratedId) {
                    // never encode coll and ts and server generated IDs
                    continue;
                }

                var fieldName = fi.getName();
                try {
                    fi.getField().setAccessible(true);
                    @SuppressWarnings("unchecked")
                    T value = obj != null ? (T) fi.getField().get(obj) : null;

                    if (fieldType == FieldType.ClientGeneratedId && value == null) {
                        // The field is a client generated ID but set to null, so assume they're doing something
                        // other than creating the object.
                        continue;
                    }

                    gen.writeFieldName(fieldName);
                    @SuppressWarnings("unchecked")
                    Codec<T> codec = fi.getCodec();
                    codec.encode(gen, value);
                } catch (IllegalAccessException e) {
                    throw new CodecException("Error accessing field: " + fi.getName(),
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

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Document, FaunaType.Null, FaunaType.Object, FaunaType.Ref};
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
                throw new CodecException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
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
            field.getField().setAccessible(true);

            if (field.getType() == Long.class) {
                field.getField().set(instance, Long.parseLong(id));
            } else if (field.getType() == String.class) {
                field.getField().set(instance, id);
            }
        }
    }

    private void trySetName(String fieldName,Object instance, UTF8FaunaParser parser) throws IllegalAccessException {
        if (parser.getCurrentTokenType() != FaunaTokenType.STRING) return;

        FieldInfo field = fieldsByName.get(fieldName);
        if (field != null) {
            String name = parser.getValueAsString();
            field.getField().setAccessible(true);
            if (field.getType() == String.class) {
                field.getField().set(instance, name);
            }
        }
    }

    private void trySetField(String fieldName, Object instance, UTF8FaunaParser parser) throws IllegalAccessException {
        FieldInfo field = fieldsByName.get(fieldName);
        if (field == null) {
            parser.skip();
        } else {
            field.getField().setAccessible(true);
            field.getField().set(instance, field.getCodec().decode(parser));
        }
    }
}
