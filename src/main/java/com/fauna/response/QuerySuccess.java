package com.fauna.response;

import com.fauna.codec.Codec;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientResponseException;
import com.fauna.exception.CodecException;
import com.fauna.response.wire.QueryResponseWire;

import java.util.Optional;

public final class QuerySuccess<T> extends QueryResponse {

    private final T data;
    private final String staticType;

    public QuerySuccess(Builder<T> builder) {
        super(builder);
        this.data = builder.data;
        // TODO: check this
        this.staticType = builder.codec.getCodecClass().getSimpleName();
    }
    /**
     * Initializes a new instance of the {@link QuerySuccess} class, decoding the query
     * response into the specified type.
     *
     * @param codec        A codec for the response data type.
     * @param response     The parsed response.
     */
    public QuerySuccess(Codec<T> codec, QueryResponseWire response) {
        super(response);

        try {
            UTF8FaunaParser reader = UTF8FaunaParser.fromString(response.getData());
            this.data = codec.decode(reader);
        } catch (CodecException exc) {
            throw new ClientResponseException("Failed to decode response.", exc);
        }
        this.staticType = response.getStaticType();
    }

    public T getData() {
        return data;
    }

    public Optional<String> getStaticType() {
        return Optional.ofNullable(staticType);
    }

}
