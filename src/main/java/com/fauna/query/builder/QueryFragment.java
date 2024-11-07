package com.fauna.query.builder;


/**
 * An abstract class serving as a base for different types of query fragments.
 * @param <T> The type of QueryFragment.
 */
public abstract class QueryFragment<T> {

    /**
     * Retrieves the value represented by this fragment.
     *
     * @return the value of this fragment.
     */
    public abstract T get();
}
