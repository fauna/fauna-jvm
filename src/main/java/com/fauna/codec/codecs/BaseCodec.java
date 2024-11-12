package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for implementing codecs to handle encoding and decoding operations for specific types.
 *
 * @param <T> the type this codec can encode or decode.
 */
public abstract class BaseCodec<T> implements Codec<T> {

    /** Set of known tag identifiers for Fauna's wire format. */
    public static final Set<String> TAGS = new HashSet<>(Arrays.asList(
            "@int", "@long", "@double", "@date", "@time", "@mod", "@ref",
            "@doc", "@set", "@object", "@bytes"
    ));

    /**
     * Returns a formatted message indicating an unexpected token encountered during decoding.
     *
     * @param token the unexpected token type encountered.
     * @return a formatted message string.
     */
    protected String unexpectedTokenExceptionMessage(final FaunaTokenType token) {
        return MessageFormat.format(
                "Unexpected token `{0}` decoding with `{1}<{2}>`", token,
                this.getClass().getSimpleName(),
                this.getCodecClass().getSimpleName());
    }

    /**
     * Returns a formatted message indicating an unsupported Fauna type encountered during decoding.
     *
     * @param type           the Fauna type encountered.
     * @param supportedTypes an array of supported Fauna types for this codec.
     * @return a formatted message string.
     */
    protected String unsupportedTypeDecodingMessage(final FaunaType type,
                                                    final FaunaType[] supportedTypes) {
        var supportedString = Arrays.toString(supportedTypes);
        return MessageFormat.format(
                "Unable to decode `{0}` with `{1}<{2}>`. Supported types for codec are {3}.",
                type, this.getClass().getSimpleName(),
                this.getCodecClass().getSimpleName(), supportedString);
    }

    /**
     * Returns a formatted message indicating an unexpected Java type encountered during decoding.
     *
     * @param type the unexpected Java type encountered.
     * @return a formatted message string.
     */
    protected String unexpectedTypeWhileDecoding(final Type type) {
        return MessageFormat.format(
                "Unexpected type `{0}` decoding with `{1}<{2}>`", type,
                this.getClass().getSimpleName(),
                this.getCodecClass().getSimpleName());
    }

    /**
     * Returns a formatted message indicating an unsupported Java type encountered during encoding.
     *
     * @param type the unsupported Java type encountered.
     * @return a formatted message string.
     */
    protected String unsupportedTypeMessage(final Type type) {
        return MessageFormat.format("Cannot encode `{0}` with `{1}<{2}>`", type,
                this.getClass(), this.getCodecClass());
    }
}
