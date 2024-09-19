package com.fauna.codec;

import java.io.IOException;

public class Helpers {

    public static <T> T decode(Codec<T> codec, String val) {
        var parser = UTF8FaunaParser.fromString(val);
        return codec.decode(parser);
    }

    public static <T> String encode(Codec<T> codec, T obj) throws IOException {
        var gen = new UTF8FaunaGenerator();
        codec.encode(gen, obj);
        return gen.serialize();
    }

    public static String getWire(FaunaType type) {
        switch (type) {
            case Int:
                return "{\"@int\":\"42\"}";
            case Long:
                return "{\"@long\":\"42\"}";
            case Double:
                return "{\"@double\":\"1.2\"}";
            case String:
                return "\"Fauna\"";
            case Date:
                return "{\"@date\":\"2020-10-10\"}";
            case Time:
                return "{\"@time\":\"2024-08-16T21:34:16.700Z\"}";
            case Boolean:
                return "true";
            case Object:
                return "{}}";
            case Ref:
                return "{\"@ref\":{}}";
            case Document:
                return "{\"@doc\":{}}";
            case Array:
                return "[]";
            case Bytes:
                return "{\"@bytes\":\"123\"}";
            case Null:
                return "null";
            case Stream:
                return "{\"@stream\":\"123\"}";
            case Module:
                return "{\"@mod\":\"Foo\"}";
            case Set:
                return "{\"@set\":{}}";
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
