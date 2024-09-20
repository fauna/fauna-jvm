package com.fauna.codec.codecs;

import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.types.BaseRef;
import com.fauna.types.DocumentRef;
import com.fauna.types.NamedDocumentRef;

public class BaseRefCodec extends BaseCodec<BaseRef> {

    public static final BaseRefCodec SINGLETON = new BaseRefCodec();

    @Override
    public BaseRef decode(UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_REF:
                return (BaseRef) decodeInternal(parser);
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(parser.getCurrentTokenType().getFaunaType(), getSupportedTypes()));
        }
    }

    private Object decodeInternal(UTF8FaunaParser parser) throws CodecException {
        var builder = new InternalDocument.Builder();

        while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_REF) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new CodecException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
            }

            String fieldName = parser.getValueAsString();
            parser.read();
            switch (fieldName) {
                case "id":
                case "name":
                case "coll":
                case "exists":
                case "cause":
                    builder = builder.withRefField(fieldName, parser);
                    break;
            }
        }

        return builder.build();
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, BaseRef obj) throws CodecException {
        gen.writeStartRef();

        if (obj instanceof DocumentRef) {
            gen.writeString("id", ((DocumentRef) obj).getId());
        } else if (obj instanceof NamedDocumentRef) {
            gen.writeString("name", ((NamedDocumentRef) obj).getName());
        }

        gen.writeModule("coll", obj.getCollection());
        gen.writeEndRef();
    }

    @Override
    public Class<BaseRef> getCodecClass() {
        return BaseRef.class;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[]{FaunaType.Null, FaunaType.Ref};
    }
}
