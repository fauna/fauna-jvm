package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Represents an {@link Optional} with a specified element type, allowing for retention of the
 * generic type {@code V} during deserialization by circumventing type erasure.
 *
 * @param <V> The element type within the optional.
 */
public final class OptionalOf<V> extends ParameterizedOf<Optional<V>> {

    /**
     * Constructs an {@code OptionalOf} instance for the specified element type.
     *
     * @param valueClass The class of the elements contained in the optional.
     */
    public OptionalOf(final Class<V> valueClass) {
        super(Optional.class, new Type[]{valueClass});
    }
}
