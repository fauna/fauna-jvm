package com.fauna.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fauna.constants.ResponseFields;
import com.fauna.mapping.MappingContext;
import com.fauna.serialization.Deserializer;
import com.fauna.serialization.UTF8FaunaParser;

import java.io.IOException;
import java.lang.reflect.Type;

public final class QuerySuccess extends QueryResponse {

    private String data;
    private String staticType;

    private Object deserialized;

    /**
     * Initializes a new instance of the {@link QuerySuccess} class.
     *
     * @param json         The parsed JSON response body.
     */
    public QuerySuccess(JsonNode json, QueryStats stats) {
        super(json, stats);
        JsonNode elem;
        if ((elem = json.get(ResponseFields.DATA_FIELD_NAME)) != null) {
            this.data = elem.toString();
        }

        if ((elem = json.get(ResponseFields.STATIC_TYPE_FIELD_NAME)) != null) {
            this.staticType = elem.asText();
        }
    }

    public <T> T to(Type type) throws IOException {
        if (deserialized != null) return (T) deserialized;

        var ser = Deserializer.generate(new MappingContext(), type);
        UTF8FaunaParser parser = new UTF8FaunaParser(data);
        parser.read();
        deserialized = ser.deserialize(parser);
        return (T) deserialized;
    }

    public Object toDynamic() throws IOException {
        if (deserialized != null) return deserialized;

        UTF8FaunaParser parser = new UTF8FaunaParser(data);
        parser.read();
        deserialized = Deserializer.DYNAMIC.deserialize(parser);
        return deserialized;
    }

    public String getStaticType() {
        return staticType;
    }

}
