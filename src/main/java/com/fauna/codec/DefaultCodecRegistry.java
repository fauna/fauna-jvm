package com.fauna.codec;

import com.fauna.codec.codecs.BaseRefCodec;
import com.fauna.codec.codecs.BoolCodec;
import com.fauna.codec.codecs.ByteArrayCodec;
import com.fauna.codec.codecs.ByteCodec;
import com.fauna.codec.codecs.CharCodec;
import com.fauna.codec.codecs.DoubleCodec;
import com.fauna.codec.codecs.FloatCodec;
import com.fauna.codec.codecs.InstantCodec;
import com.fauna.codec.codecs.IntCodec;
import com.fauna.codec.codecs.LocalDateCodec;
import com.fauna.codec.codecs.LongCodec;
import com.fauna.codec.codecs.ModuleCodec;
import com.fauna.codec.codecs.ShortCodec;
import com.fauna.codec.codecs.StringCodec;
import com.fauna.types.BaseRef;
import com.fauna.types.DocumentRef;
import com.fauna.types.Module;
import com.fauna.types.NamedDocumentRef;

import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The default codec registry for Fauna serialization and deserialization.
 * <p>
 * This registry provides pre-defined codecs for common data types, enabling
 * serialization to and deserialization from FQL.
 * </p>
 */
public final class DefaultCodecRegistry implements CodecRegistry {

    /**
     * Singleton instance of the {@code DefaultCodecRegistry} for global access.
     */
    public static final CodecRegistry SINGLETON = new DefaultCodecRegistry();

    private final ConcurrentHashMap<CodecRegistryKey, Codec<?>> codecs;

    /**
     * Initializes a new instance of {@code DefaultCodecRegistry} with predefined codecs
     * for commonly used data types.
     */
    public DefaultCodecRegistry() {
        codecs = new ConcurrentHashMap<>();
        codecs.put(CodecRegistryKey.from(String.class), StringCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(byte[].class), ByteArrayCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(boolean.class), BoolCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(Boolean.class), BoolCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(char.class), CharCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(Character.class), CharCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(byte.class), ByteCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(Byte.class), ByteCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(short.class), ShortCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(Short.class), ShortCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(Integer.class), IntCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(int.class), IntCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(Long.class), LongCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(long.class), LongCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(Float.class), FloatCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(float.class), FloatCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(Double.class), DoubleCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(double.class), DoubleCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(Instant.class), InstantCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(LocalDate.class), LocalDateCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(Module.class), ModuleCodec.SINGLETON);

        codecs.put(CodecRegistryKey.from(BaseRef.class), BaseRefCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(DocumentRef.class), BaseRefCodec.SINGLETON);
        codecs.put(CodecRegistryKey.from(NamedDocumentRef.class), BaseRefCodec.SINGLETON);
    }

    /**
     * Retrieves the codec associated with the specified key, if it exists.
     *
     * @param key The {@link CodecRegistryKey} representing the data type.
     * @param <T> The data type to be encoded or decoded.
     * @return The {@link Codec} associated with the key, or {@code null} if not found.
     */
    @Override
    public <T> Codec<T> get(final CodecRegistryKey key) {
        @SuppressWarnings("unchecked")
        var codec = (Codec<T>) codecs.get(key);
        return codec;
    }

    /**
     * Registers a new codec for the specified key in the registry.
     *
     * @param key   The {@link CodecRegistryKey} representing the data type.
     * @param codec The {@link Codec} to associate with the key.
     * @param <T>   The data type to be encoded or decoded.
     */
    @Override
    public <T> void put(final CodecRegistryKey key, final Codec<T> codec) {
        codecs.put(key, codec);
    }

    /**
     * Checks if the registry contains a codec for the specified key.
     *
     * @param key The {@link CodecRegistryKey} representing the data type.
     * @return {@code true} if a codec for the key is registered; otherwise, {@code false}.
     */
    @Override
    public boolean contains(final CodecRegistryKey key) {
        return codecs.containsKey(key);
    }
}
