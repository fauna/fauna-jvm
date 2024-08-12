package com.fauna.codec;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultCodecRegistry implements CodecRegistry {
    private static final ConcurrentHashMap<CodecRegistryKey, Codec<?>> codecs = new ConcurrentHashMap<>();

    public DefaultCodecRegistry() {}

    @Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(CodecRegistryKey key) {
        return (Codec<T>) codecs.get(key);
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
