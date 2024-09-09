package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.List;


/**
 * ListOf stores the generic parameter class to evade type erasure during deserialization.
 * @param <E> The element class of the list.
 */
public class ListOf<E> extends ParameterizedOf<List<E>> {

    public ListOf(Class<E> elementClass) {
        super(List.class, new Type[]{elementClass});
    }
}
