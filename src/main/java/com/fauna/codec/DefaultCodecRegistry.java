package com.fauna.codec;

import com.fauna.codec.codecs.*;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultCodecRegistry implements CodecRegistry {

    public static final CodecRegistry SINGLETON = new DefaultCodecRegistry();
    private final ConcurrentHashMap<CodecRegistryKey, Codec<?>> codecs;

    public DefaultCodecRegistry() {
        codecs = new ConcurrentHashMap<>();
        codecs.put(CodecRegistryKey.from(String.class), StringCodec.singleton);

        codecs.put(CodecRegistryKey.from(byte[].class), ByteArrayCodec.singleton);

        codecs.put(CodecRegistryKey.from(boolean.class), BoolCodec.singleton);
        codecs.put(CodecRegistryKey.from(Boolean.class), BoolCodec.singleton);

        codecs.put(CodecRegistryKey.from(char.class), CharCodec.singleton);
        codecs.put(CodecRegistryKey.from(Character.class), CharCodec.singleton);

        codecs.put(CodecRegistryKey.from(byte.class), ByteCodec.singleton);
        codecs.put(CodecRegistryKey.from(Byte.class), ByteCodec.singleton);

        codecs.put(CodecRegistryKey.from(short.class), ShortCodec.singleton);
        codecs.put(CodecRegistryKey.from(Short.class), ShortCodec.singleton);

        codecs.put(CodecRegistryKey.from(Integer.class), IntCodec.singleton);
        codecs.put(CodecRegistryKey.from(int.class), IntCodec.singleton);

        codecs.put(CodecRegistryKey.from(Long.class), LongCodec.singleton);
        codecs.put(CodecRegistryKey.from(long.class), LongCodec.singleton);

        codecs.put(CodecRegistryKey.from(Float.class), FloatCodec.singleton);
        codecs.put(CodecRegistryKey.from(float.class), FloatCodec.singleton);

        codecs.put(CodecRegistryKey.from(Double.class), DoubleCodec.singleton);
        codecs.put(CodecRegistryKey.from(double.class), DoubleCodec.singleton);
    }

    @Override
    public <T> Codec<T> get(CodecRegistryKey key) {
        @SuppressWarnings("unchecked")
        var codec = (Codec<T>) codecs.get(key);
        return codec;
    }

    @Override
    public <T> void put(CodecRegistryKey key, Codec<T> codec) {
        codecs.put(key, codec);
    }

    @Override
    public boolean contains(CodecRegistryKey key) {
        return codecs.containsKey(key);
    }
}
