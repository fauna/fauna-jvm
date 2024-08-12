package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.exception.NullDocumentException;
import com.fauna.serialization.*;
import com.fauna.types.*;
import com.fauna.types.Module;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicCodec extends BaseCodec<Object> {
    private final ListCodec<Object, List<Object>> list = new ListCodec<>(this);
    private final PageCodec<Object, Page<Object>> page = new PageCodec<>(this);
    private final MapCodec<Object, Map<String, Object>> map = new MapCodec<>(this);
    private final CodecProvider provider;

    public DynamicCodec(CodecProvider provider) {

        this.provider = provider;
    }

    @Override
    public Object decode(UTF8FaunaParser parser) throws IOException {
        Object value = null;
        switch (parser.getCurrentTokenType()) {
            case NULL:
                break;
            case START_OBJECT:
                value = map.decode(parser);
                break;
            case START_ARRAY:
                value = list.decode(parser);
                break;
            case START_PAGE:
                value = page.decode(parser);
                break;
            case START_REF:
                value = decodeRef(parser);
                break;
            case START_DOCUMENT:
                value = decodeDocument(parser);
                break;
            case MODULE:
                value = parser.getValueAsModule();
                break;
            case INT:
                value = parser.getValueAsInt();
                break;
            case STRING:
                value = parser.getValueAsString();
                break;
            case DATE:
                value = parser.getValueAsLocalDate();
                break;
            case TIME:
                value = parser.getValueAsTime();
                break;
            case DOUBLE:
                value = parser.getValueAsDouble();
                break;
            case LONG:
                value = parser.getValueAsLong();
                break;
            case TRUE:
            case FALSE:
                value = parser.getValueAsBoolean();
                break;
            default:
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }

        return value;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void encode(UTF8FaunaGenerator gen, Object obj) throws IOException {

        // TODO: deal with Object.class loop
        Codec codec = provider.get(obj.getClass());
        codec.encode(gen, obj);
    }

    @Override
    public Class<?> getCodecClass() {
        return null;
    }

    private Object decodeDocument(UTF8FaunaParser parser)
            throws IOException {
        Map<String, Object> data = new HashMap<>();
        String id = null;
        String name = null;
        Instant ts = null;
        Module coll = null;

        while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_DOCUMENT) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
            }

            String fieldName = parser.getValueAsString();
            parser.read();
            switch (fieldName) {
                case "id":
                    id = parser.getValueAsString();
                    break;
                case "name":
                    name = parser.getValueAsString();
                    break;
                case "ts":
                    ts = parser.getValueAsTime();
                    break;
                case "coll":
                    coll = parser.getValueAsModule();
                    break;
                default:
                    var v = this.decode(parser);
                    if (v != null) data.put(fieldName, v);
                    break;
            }
        }

        if (id != null && coll != null && ts != null) {
            if (name != null) {
                data.put("name", name);
            }
            return new Document(id, coll, ts, data);
        }

        if (name != null && coll != null && ts != null) {
            return new NamedDocument(name, coll, ts, data);
        }

        if (id != null) {
            data.put("id", id);
        }
        if (name != null) {
            data.put("name", name);
        }
        if (coll != null) {
            data.put("coll", coll);
        }
        if (ts != null) {
            data.put("ts", ts);
        }
        return data;
    }

    private Object decodeRef(UTF8FaunaParser parser)
            throws IOException {
        String id = null;
        String name = null;
        Module coll = null;
        boolean exists = true;
        String cause = null;
        Map<String, Object> allProps = new HashMap<>();

        while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_REF) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
            }

            String fieldName = parser.getValueAsString();
            parser.read();
            switch (fieldName) {
                case "id":
                    id = parser.getValueAsString();
                    allProps.put("id", id);
                    break;
                case "name":
                    name = parser.getValueAsString();
                    allProps.put("name", name);
                    break;
                case "coll":
                    coll = parser.getValueAsModule();
                    allProps.put("coll", coll);
                    break;
                case "exists":
                    exists = parser.getValueAsBoolean();
                    allProps.put("exists", exists);
                    break;
                case "cause":
                    cause = parser.getValueAsString();
                    allProps.put("cause", cause);
                    break;
                default:
                    allProps.put(fieldName, this.decode(parser));
                    break;
            }
        }

        if (id != null && coll != null) {
            if (exists) {
                return new DocumentRef(id, coll);
            }

            throw new NullDocumentException(id, coll, cause);
        }

        if (name != null && coll != null) {
            if (exists) {
                return new NamedDocumentRef(name, coll);
            }

            throw new NullDocumentException(name, coll, cause);
        }

        return allProps;
    }
}
