package com.fauna.codec;

import com.fauna.codec.codecs.BaseDocumentCodec;
import com.fauna.codec.codecs.ClassCodec;
import com.fauna.codec.codecs.DynamicCodec;
import com.fauna.codec.codecs.EnumCodec;
import com.fauna.codec.codecs.ListCodec;
import com.fauna.codec.codecs.MapCodec;
import com.fauna.codec.codecs.NullableDocumentCodec;
import com.fauna.codec.codecs.OptionalCodec;
import com.fauna.codec.codecs.PageCodec;
import com.fauna.codec.codecs.QueryArrCodec;
import com.fauna.codec.codecs.QueryCodec;
import com.fauna.codec.codecs.QueryLiteralCodec;
import com.fauna.codec.codecs.QueryObjCodec;
import com.fauna.codec.codecs.EventSourceResponseCodec;
import com.fauna.event.EventSourceResponse;
import com.fauna.codec.codecs.QueryValCodec;
import com.fauna.query.builder.Query;
import com.fauna.query.builder.QueryArr;
import com.fauna.query.builder.QueryLiteral;
import com.fauna.query.builder.QueryObj;
import com.fauna.query.builder.QueryVal;
import com.fauna.types.BaseDocument;
import com.fauna.types.Document;
import com.fauna.types.NamedDocument;
import com.fauna.types.NullableDocument;
import com.fauna.types.Page;

import java.lang.reflect.Type;
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

        registry.put(CodecRegistryKey.from(EventSourceResponse.class), new EventSourceResponseCodec());


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
    public <T> Codec<T> get(Class<T> clazz, Type[] typeArgs) {
        CodecRegistryKey key = CodecRegistryKey.from(clazz, typeArgs);

        if (!registry.contains(key)) {
            var codec = generate(clazz, typeArgs);
            registry.put(key, codec);
        }

        return registry.get(key);
    }

    @SuppressWarnings({"unchecked"})
    private <T,E> Codec<T> generate(Class<T> clazz, Type[] typeArgs) {
        if (Map.class.isAssignableFrom(clazz)) {
            var ta = typeArgs == null || typeArgs.length <= 1 ? Object.class : typeArgs[1];
            Codec<?> valueCodec = this.get((Class<?>) ta, null);

            return (Codec<T>) new MapCodec<E,Map<String,E>>((Codec<E>) valueCodec);
        }

        var ta = typeArgs == null || typeArgs.length == 0 ? Object.class : typeArgs[0];

        if (List.class.isAssignableFrom(clazz)) {
            Codec<?> elemCodec = this.get((Class<?>) ta, null);

            return (Codec<T>) new ListCodec<E,List<E>>((Codec<E>) elemCodec);
        }

        if (clazz == Optional.class) {
            Codec<?> valueCodec = this.get((Class<?>) ta, null);
            return (Codec<T>) new OptionalCodec<E,Optional<E>>((Codec<E>) valueCodec);
        }

        if (clazz == Page.class) {
            Codec<?> valueCodec = this.get((Class<?>) ta, null);
            return (Codec<T>) new PageCodec<E,Page<E>>((Codec<E>) valueCodec);
        }

        if (clazz == NullableDocument.class) {
            Codec<?> valueCodec = this.get((Class<?>) ta, null);
            return (Codec<T>) new NullableDocumentCodec<E, NullableDocument<E>>((Codec<E>) valueCodec);
        }

        if (clazz.isEnum()) {
            return new EnumCodec<>(clazz);
        }

        return new ClassCodec<>(clazz, this);
    }
}
