package com.fauna.codec.codecs;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
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
    public BaseDocument decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_REF:
                var o = BaseRefCodec.SINGLETON.decode(parser);
                // if we didn't throw a null ref, we can't deal with it
                throw new ClientException(unexpectedTypeWhileDecoding(o.getClass()));
            case START_DOCUMENT:
                return (BaseDocument) decodeInternal(parser);
            default:
                throw new ClientException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    private Object decodeInternal(UTF8FaunaParser parser) throws IOException {
        var builder = new InternalDocument.Builder();
        var valueCodec = provider.get(Object.class);

        while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_DOCUMENT) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
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
    public void encode(UTF8FaunaGenerator gen, BaseDocument obj) throws IOException {
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
        return new FaunaType[]{FaunaType.Null, FaunaType.Ref, FaunaType.Document};
    }
}
