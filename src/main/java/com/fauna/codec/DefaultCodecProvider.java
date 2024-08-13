package com.fauna.codec;

public class DefaultCodecProvider implements CodecProvider {

    @Override
    public <T> Codec<T> get(CodecRegistry registry, Class<T> clazz) {
        return get(registry, clazz, null);
    }

    @Override
    public <T, K> Codec<T> get(CodecRegistry registry, Class<T> clazz, Class<K> subClazz) {
        CodecRegistryKey key = CodecRegistryKey.from(clazz, subClazz);

        if (!registry.contains(key)) {
            Codec<T> codec = generate(clazz, subClazz);
            registry.add(key, codec);
        }

        return registry.get(key);
    }

    private <T,K> Codec<T> generate(Class<T> clazz, Class<K> subClazz) {
        return null;
    }
}
