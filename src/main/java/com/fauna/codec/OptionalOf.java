package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.Optional;


/**
 * OptionalOf stores the generic parameter class to evade type erasure during deserialization.
 *
 * @param <V> The element class of the list.
 */
public class OptionalOf<V> extends ParameterizedOf<Optional<V>> {

    public OptionalOf(Class<V> valueClass) {
        super(Optional.class, new Type[] {valueClass});
    }
}
