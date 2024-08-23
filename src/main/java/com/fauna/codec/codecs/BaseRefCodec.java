package com.fauna.codec.codecs;

import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.types.*;

import java.io.IOException;

public class BaseRefCodec extends BaseCodec<BaseRef> {

    public static final BaseRefCodec SINGLETON = new BaseRefCodec();

    @Override
    public BaseRef decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_REF:
                return (BaseRef) decodeInternal(parser);
            default:
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }
    }

    private Object decodeInternal(UTF8FaunaParser parser) throws IOException {
        var builder = new InternalDocument.Builder();

        while (parser.read() && parser.getCurrentTokenType() != FaunaTokenType.END_REF) {
            if (parser.getCurrentTokenType() != FaunaTokenType.FIELD_NAME) {
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
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
    public void encode(UTF8FaunaGenerator gen, BaseRef obj) throws IOException {
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
}