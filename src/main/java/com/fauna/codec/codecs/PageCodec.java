package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;
import com.fauna.codec.UTF8FaunaGenerator;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.CodecException;
import com.fauna.types.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Codec for encoding and decoding Fauna's paginated results.
 *
 * @param <E> The type of elements in the page.
 * @param <L> The type of the Page (Page<E>).
 */
public final class PageCodec<E, L extends Page<E>> extends BaseCodec<L> {

    private final Codec<E> elementCodec;
    private final Codec<List<E>> listCodec;

    /**
     * Constructs a {@code PageCodec} with the specified {@code Codec}.
     *
     * @param elementCodec The codec to use for elements of the page.
     */
    public PageCodec(final Codec<E> elementCodec) {
        this.elementCodec = elementCodec;
        this.listCodec = new ListCodec<>(elementCodec);
    }

    @Override
    public L decode(final UTF8FaunaParser parser) throws CodecException {
        switch (parser.getCurrentTokenType()) {
            case NULL:
                return null;
            case START_PAGE:
                return decodePage(parser, FaunaTokenType.END_PAGE);
            case START_OBJECT:
                // Handles a special case where calls with Set.paginate do not
                // return an object with a @set tag.
                return decodePage(parser, FaunaTokenType.END_OBJECT);
            case START_ARRAY:
                @SuppressWarnings("unchecked")
                L res = (L) new Page<>(listCodec.decode(parser), null);
                return res;
            case START_DOCUMENT:
            case START_REF:
            case STRING:
            case BYTES:
            case INT:
            case LONG:
            case DOUBLE:
            case DATE:
            case TIME:
            case TRUE:
            case FALSE:
            case MODULE:
                // In the event the user requests a Page<T> but the query just returns T
                return wrapInPage(parser);
            default:
                throw new CodecException(this.unsupportedTypeDecodingMessage(
                        parser.getCurrentTokenType().getFaunaType(),
                        getSupportedTypes()));
        }
    }

    @Override
    public void encode(final UTF8FaunaGenerator gen, final L obj) throws CodecException {
        if (obj == null) {
            gen.writeNullValue();
        } else {
            throw new CodecException(
                    this.unsupportedTypeMessage(obj.getClass()));
        }
    }

    @Override
    public Class<?> getCodecClass() {
        return elementCodec.getCodecClass();
    }

    private L decodePage(final UTF8FaunaParser parser, final FaunaTokenType endToken)
            throws CodecException {

        parser.read();
        if (parser.getCurrentTokenType() == FaunaTokenType.STRING) {
            return handleUnmaterialized(parser, endToken);
        } else {
            return handleMaterialized(parser, endToken);
        }
    }

    private L handleMaterialized(final UTF8FaunaParser parser, final FaunaTokenType endToken) {
        List<E> data = null;
        String after = null;
        do {
            String fieldName = parser.getValueAsString();
            parser.read();

            switch (fieldName) {
                case "data":
                    data = listCodec.decode(parser);
                    break;
                case "after":
                    after = parser.getValueAsString();
                    break;
                default:
                    break;
            }
        } while (parser.read() && parser.getCurrentTokenType() != endToken);

        //noinspection unchecked
        return (L) new Page<>(data, after);
    }

    private L handleUnmaterialized(final UTF8FaunaParser parser, final FaunaTokenType endToken) {
        var after = parser.getValueAsString();
        parser.read();

        if (parser.getCurrentTokenType() != endToken) {
            throw new CodecException(unexpectedTokenExceptionMessage(parser.getCurrentTokenType()));
        }

        //noinspection unchecked
        return (L) new Page<>(new ArrayList<>(), after);

    }

    private L wrapInPage(final UTF8FaunaParser parser) throws CodecException {
        E elem = this.elementCodec.decode(parser);
        @SuppressWarnings("unchecked")
        L res = (L) new Page<>(List.of(elem), null);
        return res;
    }

    @Override
    public FaunaType[] getSupportedTypes() {
        return new FaunaType[] {FaunaType.Array, FaunaType.Boolean,
                FaunaType.Bytes, FaunaType.Date, FaunaType.Double,
                FaunaType.Document, FaunaType.Int, FaunaType.Long,
                FaunaType.Module, FaunaType.Null, FaunaType.Object,
                FaunaType.Ref, FaunaType.Set, FaunaType.String, FaunaType.Time};
    }
}
