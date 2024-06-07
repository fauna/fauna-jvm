package com.fauna.query.builder;

/**
 * An abstract class serving as a base for different types of query fragments.
 */
abstract class Fragment {

    /**
     * Retrieves the value represented by this fragment.
     *
     * @return the value of this fragment.
     */
    public abstract Object get();

}
