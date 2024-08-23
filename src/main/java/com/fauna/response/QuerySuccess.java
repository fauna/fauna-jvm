package com.fauna.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fauna.codec.Codec;
import com.fauna.codec.UTF8FaunaParser;
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
    public QuerySuccess(Codec<T> codec, QueryResponseInternal response)
            throws IOException {
        super(response);

        UTF8FaunaParser reader = new UTF8FaunaParser(response.data);
        this.data = codec.decode(reader);
        this.staticType = response. staticType;
    }

    public T getData() {
        return data;
    }

    public Optional<String> getStaticType() {
        return Optional.ofNullable(staticType);
    }

}
