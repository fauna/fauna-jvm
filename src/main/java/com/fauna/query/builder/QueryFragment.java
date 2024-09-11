package com.fauna.query.builder;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;

import java.io.IOException;

/**
 * An abstract class serving as a base for different types of query fragments.
 */
public abstract class QueryFragment<T> {

    /**
     * Retrieves the value represented by this fragment.
     *
     * @return the value of this fragment.
     */
    public abstract T get();
}
