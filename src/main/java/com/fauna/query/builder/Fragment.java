package com.fauna.query.builder;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;

import java.io.IOException;
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

    public abstract void encode(UTF8FaunaGenerator gen, CodecProvider provider) throws IOException;

}
