package com.fauna.codec;

import com.fauna.codec.codecs.BaseDocumentCodec;
import com.fauna.codec.codecs.ClassCodec;
import com.fauna.codec.codecs.DynamicCodec;
import com.fauna.codec.codecs.EnumCodec;
import com.fauna.codec.codecs.ListCodec;
import com.fauna.codec.codecs.MapCodec;
import com.fauna.codec.codecs.NullableCodec;
import com.fauna.codec.codecs.OptionalCodec;
import com.fauna.codec.codecs.PageCodec;
import com.fauna.codec.codecs.QueryArrCodec;
import com.fauna.codec.codecs.QueryCodec;
import com.fauna.codec.codecs.QueryLiteralCodec;
import com.fauna.codec.codecs.QueryObjCodec;
import com.fauna.codec.codecs.StreamTokenResponseCodec;
import com.fauna.query.StreamTokenResponse;
import com.fauna.codec.codecs.QueryValCodec;
import com.fauna.query.builder.Query;
import com.fauna.query.builder.QueryArr;
import com.fauna.query.builder.QueryLiteral;
import com.fauna.query.builder.QueryObj;
import com.fauna.query.builder.QueryVal;
import com.fauna.types.BaseDocument;
import com.fauna.types.Document;
import com.fauna.types.NamedDocument;
import com.fauna.types.Nullable;
import com.fauna.types.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultCodecProvider implements CodecProvider {

    private final CodecRegistry registry;

    public static final CodecProvider SINGLETON = new DefaultCodecProvider(DefaultCodecRegistry.SINGLETON);

    public DefaultCodecProvider(CodecRegistry registry) {
        registry.put(CodecRegistryKey.from(Object.class), new DynamicCodec(this));

        registry.put(CodecRegistryKey.from(Query.class), new QueryCodec(this));
        registry.put(CodecRegistryKey.from(QueryObj.class), new QueryObjCodec(this));
        registry.put(CodecRegistryKey.from(QueryArr.class), new QueryArrCodec(this));
        registry.put(CodecRegistryKey.from(QueryVal.class), new QueryValCodec(this));
        registry.put(CodecRegistryKey.from(QueryLiteral.class), new QueryLiteralCodec());

        registry.put(CodecRegistryKey.from(StreamTokenResponse.class), new StreamTokenResponseCodec());


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
        if (List.class.isAssignableFrom(clazz)) {
            Codec<E> elemCodec = this.get(ta, null);

            return (Codec<T>) new ListCodec<E,List<E>>(elemCodec);
        }

        if (Map.class.isAssignableFrom(clazz)) {
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

        if (clazz.isEnum()) {
            return new EnumCodec<>(clazz);
        }

        return new ClassCodec<>(clazz, this);
    }
}
