package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.query.StreamTokenResponse;
import com.fauna.types.Document;
import com.fauna.types.DocumentRef;
import com.fauna.types.Page;

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
    public Object decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case BYTES:
                return provider.get(byte[].class).decode(parser);
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
            case STREAM:
                return provider.get(StreamTokenResponse.class).decode(parser);
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

        throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void encode(UTF8FaunaGenerator gen, Object obj) throws CodecException {

        // TODO: deal with Object.class loop
        @SuppressWarnings("rawtypes")
        Codec codec = provider.get(obj.getClass());
        codec.encode(gen, obj);
    }

    @Override
    public Class<Object> getCodecClass() {
        return Object.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Array, FaunaType.Boolean, FaunaType.Bytes, FaunaType.Date, FaunaType.Double, FaunaType.Document, FaunaType.Int, FaunaType.Long, FaunaType.Module, FaunaType.Null, FaunaType.Object,  FaunaType.Ref, FaunaType.Set, FaunaType.Stream, FaunaType.String, FaunaType.Time};
    }
}
