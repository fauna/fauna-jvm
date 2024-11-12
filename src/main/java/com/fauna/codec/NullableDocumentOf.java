package com.fauna.codec;

import com.fauna.types.NullableDocument;

import java.lang.reflect.Type;

/**
 * Represents a {@link NullableDocument} with a specified value type, allowing for retention
 * of the generic type {@code E} during deserialization by circumventing type erasure.
 *
 * @param <E> The type of the value contained in the {@code NullableDocument}.
 */
public final class NullableDocumentOf<E> extends ParameterizedOf<NullableDocument<E>> {

    /**
     * Constructs a {@code NullableDocumentOf} instance for the specified value type.
     *
     * @param valueClass The class of the value contained in the {@code NullableDocument}.
     */
    public NullableDocumentOf(final Class<E> valueClass) {
        super(NullableDocument.class, new Type[]{valueClass});
    }
}
