package com.fauna.codec;

import com.fauna.types.Nullable;

import java.lang.reflect.Type;


/**
 * NullableOf stores the generic parameter class to evade type erasure during deserialization.
 * @param <E> The value class of the list.
 */
public class NullableOf<E> extends ParameterizedOf<Nullable<E>> {
    public NullableOf(Class<E> valueClass) {
        super(Nullable.class, new Type[]{valueClass});
    }
}