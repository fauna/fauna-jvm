package com.fauna.client;

import com.fauna.serialization.Serializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FaunaRequestTest {

    @Test
    public void testFaunaRequestWithValue() throws IOException {
        FaunaRequest request = new FaunaRequest(fql("Posts.where(.id = ${id})", Map.of("id", 1)));
        String serialized = Serializer.serialize(request);
        // Is it correct that this value should be tagged i.e. {"@int": 1} vs just {"value": 1}?
        assertEquals("{\"query\":{\"fql\":[\"Posts.where(.id = \",{\"value\":{\"@int\":\"1\"}},\")\"]}}",
                serialized);
    }

    @Test
    public void testSingleFragmentFaunaRequest() throws IOException {
        FaunaRequest request = new FaunaRequest(fql("Posts.where(.id = 1)"));
        String serialized = Serializer.serialize(request);
        assertEquals("{\"query\":{\"fql\":[\"Posts.where(.id = 1)\"]}}", serialized);
    }
}
