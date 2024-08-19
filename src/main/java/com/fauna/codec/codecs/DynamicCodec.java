package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.serialization.*;
import com.fauna.types.*;

import java.io.IOException;
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
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_OBJECT:
                return map.decode(parser);
            case START_ARRAY:
                return list.decode(parser);
            case START_PAGE:
                return page.decode(parser);
            case START_REF:
                return provider.get(DocumentRef.class).decode(parser);
            case START_DOCUMENT:
                return provider.get(Document.class).decode(parser);
            case MODULE:
                return parser.getValueAsModule();
            case INT:
                return parser.getValueAsInt();
            case STRING:
                return parser.getValueAsString();
            case DATE:
                return parser.getValueAsLocalDate();
            case TIME:
                return parser.getValueAsTime();
            case DOUBLE:
                return parser.getValueAsDouble();
            case LONG:
                return parser.getValueAsLong();
            case TRUE:
            case FALSE:
                return parser.getValueAsBoolean();
        }

        throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void encode(UTF8FaunaGenerator gen, Object obj) throws IOException {

        // TODO: deal with Object.class loop
        @SuppressWarnings("rawtypes")
        Codec codec = provider.get(obj.getClass());
        codec.encode(gen, obj);
    }

    @Override
    public Class<Object> getCodecClass() {
        return Object.class;
    }

    private Object decodeRef(UTF8FaunaParser parser)
            throws IOException {

        var builder = new InternalDocument.Builder();

        while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_REF) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
            }

            String fieldName = parser.getValueAsString();
            parser.read();
            builder = builder.withRefField(fieldName, parser);
        }

        return builder.build();
    }
}
