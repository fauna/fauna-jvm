package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.FaunaTokenType;
import com.fauna.codec.FaunaType;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseCodec<T> implements Codec<T> {

    public static Set<String> TAGS = new HashSet<>(Arrays.asList(
            "@int", "@long", "@double", "@date", "@time", "@mod", "@ref", "@doc", "@set", "@object", "@bytes"
    ));

    protected String unexpectedTokenExceptionMessage(FaunaTokenType token) {
        return MessageFormat.format("Unexpected token `{0}` decoding with `{1}<{2}>`", token, this.getClass().getSimpleName(), this.getCodecClass().getSimpleName());
    }

    protected String unsupportedTypeDecodingMessage(FaunaType type, FaunaType[] supportedTypes) {
        var supportedString = Arrays.toString(supportedTypes);
        return MessageFormat.format("Unable to decode `{0}` with `{1}<{2}>`. Supported types for codec are {3}.", type, this.getClass().getSimpleName(), this.getCodecClass().getSimpleName(), supportedString);
    }

    protected String unexpectedTypeWhileDecoding(Type type) {
        return MessageFormat.format("Unexpected type `{0}` decoding with `{1}<{2}>`", type, this.getClass().getSimpleName(), this.getCodecClass().getSimpleName());
    }

    protected String unsupportedTypeMessage(Type type){
        return MessageFormat.format("Cannot encode `{0}` with `{1}<{2}>`", type, this.getClass(), this.getCodecClass());
    }
}
