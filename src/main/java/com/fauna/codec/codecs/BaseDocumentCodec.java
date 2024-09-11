package com.fauna.codec.codecs;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.types.BaseDocument;
import com.fauna.types.Document;
import com.fauna.types.NamedDocument;

import java.io.IOException;

public class BaseDocumentCodec extends BaseCodec<BaseDocument> {

    private final CodecProvider provider;

    public BaseDocumentCodec(CodecProvider provider) {
        this.provider = provider;
    }

    @Override
    public BaseDocument decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_REF:
                var o = BaseRefCodec.SINGLETON.decode(parser);
                // if we didn't throw a null ref, we can't deal with it
                throw new CodecException(unexpectedTypeWhileDecoding(o.getClass()));
            case START_DOCUMENT:
                return (BaseDocument) decodeInternal(parser);
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    private Object decodeInternal(UTF8FaunaParser parser) throws CodecException {
        var builder = new InternalDocument.Builder();
        var valueCodec = provider.get(Object.class);

        while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_DOCUMENT) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new CodecException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
            }

            String fieldName = parser.getValueAsString();
            parser.read();
            switch (fieldName) {
                case "id":
                case "name":
                case "ts":
                case "coll":
                    builder = builder.withDocField(fieldName, parser);
                    break;
                default:
                    var v = valueCodec.decode(parser);
                    if (v != null) {
                        builder = builder.withDataField(fieldName, v);
                    }
                    break;
            }
        }

        return builder.build();
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, BaseDocument obj) throws CodecException {
        gen.writeStartRef();

        if (obj instanceof Document) {
            gen.writeString("id", ((Document) obj).getId());
        } else if (obj instanceof NamedDocument) {
            gen.writeString("name", ((NamedDocument) obj).getName());
        }

        gen.writeModule("coll", obj.getCollection());
        gen.writeEndRef();
    }

    @Override
    public Class<BaseDocument> getCodecClass() {
        return BaseDocument.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Document, FaunaType.Null, FaunaType.Ref};
    }
}
