package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.ClientException;
import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;
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
            return;
        }

        throw new ClientException(this.unsupportedTypeMessage(obj.getClass()));
    }

    @Override
    public Class<?> getCodecClass() {
        return Page.class;
    }


    @SuppressWarnings("unchecked")
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

        return (L) new Page<>(data, after);
    }

    @SuppressWarnings("unchecked")
    private L wrapDocumentInPage(UTF8FaunaParser parser) throws IOException {
        E elem = this.elementCodec.decode(parser);
        return (L) new Page<>(List.of(elem), null);
    }
}
