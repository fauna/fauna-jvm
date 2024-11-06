package com.fauna.codec;

import com.fauna.types.NullableDocument;

import java.lang.reflect.Type;


/**
 * NullableDocumentOf stores the generic parameter class to evade type erasure during deserialization.
 *
 * @param <E> The value class of the list.
 */
public class NullableDocumentOf<E>
        extends ParameterizedOf<NullableDocument<E>> {
    public NullableDocumentOf(Class<E> valueClass) {
        super(NullableDocument.class, new Type[] {valueClass});
    }
}