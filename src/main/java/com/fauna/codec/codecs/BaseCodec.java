package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.enums.FaunaTokenType;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseCodec<T> implements Codec<T> {

    protected Set<String> TAGS = new HashSet<>(Arrays.asList(
            "@int", "@long", "@double", "@date", "@time", "@mod", "@ref", "@doc", "@set", "@object"
    ));

    protected String unexpectedTokenExceptionMessage(FaunaTokenType token) {
        return MessageFormat.format("Unexpected token `{0}` decoding with `{1}`", token, this.getClass());
    }

    protected String unsupportedTypeMessage(Type type){
        return MessageFormat.format("Cannot encode `{0}` with `{1}`", type, this.getClass());
    }
}
