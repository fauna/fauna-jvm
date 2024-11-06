package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.query.builder.Query;
import com.fauna.query.builder.QueryFragment;
import com.fauna.query.builder.QueryObj;
import com.fauna.query.builder.QueryVal;
import com.fauna.response.QuerySuccess;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.fauna.codec.Generic.mapOf;
import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EQueryObjTest {
    public static final FaunaClient c = Fauna.local();

    @Test
    public void query_mapWithEmbeddedQuery() {
        Query subq = fql("4 + 2");
        Map<String, QueryFragment> obj = Map.of("k", subq);

        Query q = fql("${obj}", Map.of("obj", QueryObj.of(obj)));
        QuerySuccess<Map<String, Integer>> res =
                c.query(q, mapOf(Integer.class));

        assertEquals(Map.of("k", 6), res.getData());
    }

    @Test
    public void query_mapMixedWithEmbeddedQuery() {
        Query subq = fql("4 + 2");
        Map<String, QueryFragment> obj =
                Map.of("k", subq, "k2", new QueryVal<>(42));

        Query q = fql("${obj}", Map.of("obj", QueryObj.of(obj)));
        QuerySuccess<Map<String, Integer>> res =
                c.query(q, mapOf(Integer.class));

        assertEquals(Map.of("k", 6, "k2", 42), res.getData());
    }

    @Test
    public void query_mapWithNestedEmbeddedQuery() {
        Query subq = fql("4 + 2");
        var nest = QueryObj.of(Map.of("k", subq));
        var obj = Map.of("k", nest);
        Query q = fql("${obj}", Map.of("obj", QueryObj.of(obj)));
        var res = c.query(q);

        assertEquals(Map.of("k", Map.of("k", 6)), res.getData());
    }
}
