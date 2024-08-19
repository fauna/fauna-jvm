package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.types.Page;

import java.io.IOException;
import java.util.List;

public class PageCodec<E,L extends Page<E>> extends BaseCodec<L> {

    private final Codec<E> elementCodec;
    private final Codec<List<E>> listCodec;

    public PageCodec(Codec<E> elementCodec) {
        this.elementCodec = elementCodec;
        this.listCodec = new ListCodec<>(elementCodec);
    }

    @Override
    public L decode(UTF8FaunaParser parser) throws IOException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_PAGE:
                return decodePage(parser, FaunaTokenType.END_PAGE);
            case START_OBJECT:
                return decodePage(parser, FaunaTokenType.END_OBJECT);
            case START_DOCUMENT:
                return wrapDocumentInPage(parser);
            default:
                throw new ClientException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }
    }

    @Override
    public void encode(UTF8FaunaGenerator gen, L obj) throws IOException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            throw new ClientException(this.unsupportedTypeMessage(obj.getClass()));
        }
    }

    @Override
    public Class<?> getCodecClass() {
        return Page.class;
    }


    private L decodePage(UTF8FaunaParser parser, FaunaTokenType endToken) throws IOException {
        List<E> data = null;
        String after = null;

        while (parser.read() && parser.getCurrentTokenType() != endToken) {
            String fieldName = parser.getValueAsString();
            parser.read();

            switch (fieldName) {
                case "data":
                    data = listCodec.decode(parser);
                    break;
                case "after":
                    after = parser.getValueAsString();
                    break;
            }
        }

        if (data == null) {
            throw new ClientException("No page data found while deserializing into Page<>");
        }

        @SuppressWarnings("unchecked")
        L res = (L) new Page<>(data, after);
        return res;
    }

    private L wrapDocumentInPage(UTF8FaunaParser parser) throws IOException {
        E elem = this.elementCodec.decode(parser);
        @SuppressWarnings("unchecked")
        L res = (L) new Page<>(List.of(elem), null);
        return res;
    }
}
