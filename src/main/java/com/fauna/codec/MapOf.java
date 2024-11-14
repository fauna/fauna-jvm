package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Represents a {@link Map} with {@link String} keys and a specified value type, allowing for
 * retention of the generic type {@code V} during deserialization by circumventing type erasure.
 *
 * @param <K> The type of keys maintained by the map (constrained to {@link String}).
 * @param <V> The type of mapped values.
 */
public final class MapOf<K extends String, V> extends ParameterizedOf<Map<K, V>> {

    /**
     * Constructs a {@code MapOf} instance for a map with {@link String} keys and the specified value type.
     *
     * @param valueClass The class of the map's values.
     */
    public MapOf(final Class<V> valueClass) {
        super(Map.class, new Type[]{String.class, valueClass});
    }
}
