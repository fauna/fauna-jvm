package com.fauna.response;

import com.fauna.codec.Codec;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientResponseException;
import com.fauna.response.wire.QueryResponseWire;

import java.io.IOException;
import java.util.Optional;

public final class QuerySuccess<T> extends QueryResponse {

    private final T data;
    private final String staticType;

    /**
     * Initializes a new instance of the {@link QuerySuccess} class, decoding the query
     * response into the specified type.
     *
     * @param codec        A codec for the response data type.
     * @param response     The parsed response.
     */
    public QuerySuccess(Codec<T> codec, QueryResponseWire response)
            throws IOException {
        super(response);

        UTF8FaunaParser reader = new UTF8FaunaParser(response.getData());
        this.data = codec.decode(reader);

        this.staticType = response.getStaticType();
    }

    public T getData() {
        return data;
    }

    public Optional<String> getStaticType() {
        return Optional.ofNullable(staticType);
    }

}
