package com.fauna.client;

import com.fauna.codec.DefaultCodecProvider;
import com.fauna.query.builder.Query;
import com.fauna.codec.UTF8FaunaGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FaunaRequestTest {

    private String encode(Query q) throws IOException {
        var gen = new UTF8FaunaGenerator();
        q.encode(gen, DefaultCodecProvider.SINGLETON);
        return gen.serialize();
    }
    @Test
    public void testFaunaRequestWithValue() throws IOException {
        Query q = fql("Posts.where(.id = ${id})", Map.of("id", 1));
        String serialized = encode(q);
        // Is it correct that this value should be tagged i.e. {"@int": 1} vs just {"value": 1}?
        assertEquals("{\"query\":{\"fql\":[\"Posts.where(.id = \",{\"value\":{\"@int\":\"1\"}},\")\"]}}",
                serialized);
    }

    @Test
    public void testSingleFragmentFaunaRequest() throws IOException {
        String serialized = encode(fql("Posts.where(.id = 1)"));
        assertEquals("{\"query\":{\"fql\":[\"Posts.where(.id = 1)\"]}}", serialized);
    }
}
