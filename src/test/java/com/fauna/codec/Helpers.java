package com.fauna.codec;

import com.fauna.serialization.UTF8FaunaGenerator;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;

public class Helpers {

    public static <T> T decode(Codec<T> codec, String val) throws IOException {
        var parser = new UTF8FaunaParser(val);
        return codec.decode(parser);
    }

    public static <T> String encode(Codec<T> codec, T obj) throws IOException {
        var gen = new UTF8FaunaGenerator();
        codec.encode(gen, obj);
        return gen.serialize();
    }
}
