package com.fauna.codec;

import com.fauna.codec.codecs.*;

import java.util.List;
import java.util.Map;

public class DefaultCodecProvider implements CodecProvider {


    private final CodecRegistry registry;

    public DefaultCodecProvider(CodecRegistry registry) {
        var dynamic = new DynamicCodec(this);
        registry.put(CodecRegistryKey.from(String.class), StringCodec.singleton);
        registry.put(CodecRegistryKey.from(Integer.class), IntCodec.singleton);
        registry.put(CodecRegistryKey.from(int.class), IntCodec.singleton);
        registry.put(CodecRegistryKey.from(Object.class), dynamic);
        this.registry = registry;
    }

    public <T> Codec<T> get(Class<T> clazz) {
        return get(clazz, null);
    }

    @Override
    public <T,E> Codec<T> get(Class<T> clazz, Class<E> typeArg) {
        CodecRegistryKey key = CodecRegistryKey.from(clazz, typeArg);

        if (!registry.contains(key)) {
            var codec = generate(clazz, typeArg);
            registry.put(key, codec);
        }

        return registry.get(key);
    }

    @SuppressWarnings({"unchecked"})
    private <T,E> Codec<T> generate(Class<T> clazz, Class<E> typeArg) {
        var ta = (Class<E>) (typeArg == null ? Object.class : typeArg);
        if (clazz == List.class) {
            Codec<E> elemCodec = this.get(ta, null);

            return (Codec<T>) new ListCodec<E,List<E>>(elemCodec);
        }

        if (clazz == Map.class) {
            Codec<E> valueCodec = this.get(ta, null);

            return (Codec<T>) new MapCodec<E,Map<String,E>>(valueCodec);
        }

        return new ClassCodec<>(clazz, this);
    }
}
