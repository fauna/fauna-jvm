package com.fauna.codec;

import com.fauna.codec.codecs.*;
import com.fauna.types.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultCodecProvider implements CodecProvider {

    private final CodecRegistry registry;

    public static final CodecProvider SINGLETON = new DefaultCodecProvider(DefaultCodecRegistry.SINGLETON);

    public DefaultCodecProvider(CodecRegistry registry) {
        registry.put(CodecRegistryKey.from(Object.class), new DynamicCodec(this));

        var bdc = new BaseDocumentCodec(this);
        registry.put(CodecRegistryKey.from(BaseDocument.class), bdc);
        registry.put(CodecRegistryKey.from(Document.class), bdc);
        registry.put(CodecRegistryKey.from(NamedDocument.class), bdc);

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

        if (clazz == Optional.class) {
            Codec<E> valueCodec = this.get(ta, null);
            return (Codec<T>) new OptionalCodec<E,Optional<E>>(valueCodec);
        }

        if (clazz == Page.class) {
            Codec<E> valueCodec = this.get(ta, null);
            return (Codec<T>) new PageCodec<E,Page<E>>(valueCodec);
        }

        if (clazz == Nullable.class) {
            Codec<E> valueCodec = this.get(ta, null);
            return (Codec<T>) new NullableCodec<E,Nullable<E>>(valueCodec);
        }

        return new ClassCodec<>(clazz, this);
    }
}
