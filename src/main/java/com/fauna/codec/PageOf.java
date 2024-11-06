package com.fauna.codec;

import com.fauna.types.Page;

import java.lang.reflect.Type;

/**
 * PageOf stores the generic parameter class to evade type erasure during deserialization.
 *
 * @param <V> The element class of the page.
 */
public class PageOf<V> extends ParameterizedOf<Page<V>> {

    public PageOf(Class<V> valueClass) {
        super(Page.class, new Type[] {valueClass});
    }
}