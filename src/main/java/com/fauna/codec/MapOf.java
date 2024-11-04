package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * MapOf stores the generic parameter class to evade type erasure during decoding.
 *
 * @param <V> The value class of the Map.
 */
public class MapOf<K extends String, V> extends ParameterizedOf<Map<K, V>> {

    public MapOf(Class<V> valueClass) {
        super(Map.class, new Type[] {String.class, valueClass});
    }
}
