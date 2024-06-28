package com.fauna.query.builder;

import java.io.Serializable;

/**
 * An abstract class serving as a base for different types of query fragments.
 */
public abstract class Fragment implements Serializable {

    /**
     * Retrieves the value represented by this fragment.
     *
     * @return the value of this fragment.
     */
    public abstract Object get();

}
