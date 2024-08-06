package com.fauna.codec;

import java.util.HashMap;

public class DefaultCodecRegistry implements CodecRegistry {

    private static final HashMap<CodecRegistryKey, Codec<?>> codecs = new HashMap<>();

    public DefaultCodecRegistry() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(CodecRegistryKey key) {
        return (Codec<T>) codecs.get(key);
    }

    @Override
    public <T> void add(CodecRegistryKey key, Codec<T> codec) {
        if (codecs.containsKey(key)) return;
        codecs.put(key, codec);
    }

    @Override
    public boolean contains(CodecRegistryKey key) {
        return codecs.containsKey(key);
    }
}
