package com.fauna.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fauna.constants.ResponseFields;
import com.fauna.interfaces.IDeserializer;
import com.fauna.codec.UTF8FaunaParser;
import java.io.IOException;
import java.util.Optional;

public final class QuerySuccess<T> extends QueryResponse {

    private T data;
    private String staticType;

    /**
     * Initializes a new instance of the {@link QuerySuccess} class, deserializing the query
     * response into the specified type.
     *
     * @param deserializer A deserializer for the response data type.
     * @param json         The parsed JSON response body.
     */
    public QuerySuccess(IDeserializer<T> deserializer, JsonNode json, QueryStats stats)
        throws IOException {
        super(json, stats);
        JsonNode elem;
        if ((elem = json.get(ResponseFields.DATA_FIELD_NAME)) != null) {
            // FIXME: avoid converting the parsed `elem` to a string and re-parsing the JSON.
            UTF8FaunaParser reader = new UTF8FaunaParser(elem.toString());
            this.data = deserializer.deserialize(reader);
        }

        if ((elem = json.get(ResponseFields.STATIC_TYPE_FIELD_NAME)) != null) {
            this.staticType = elem.asText();
        } else {
            this.staticType = null;
        }
    }

    public T getData() {
        return data;
    }

    public Optional<String> getStaticType() {
        return Optional.ofNullable(staticType);
    }

}
