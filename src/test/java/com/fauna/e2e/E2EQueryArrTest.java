package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.query.builder.Query;
import com.fauna.query.builder.QueryArr;
import com.fauna.response.QuerySuccess;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.fauna.codec.Parameterized.listOf;
import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EQueryArrTest {
    public static final FaunaClient c = Fauna.local();

    @Test
    public void query_listWithEmbeddedQueries() {
        var obj = QueryArr.of(List.of(fql("2*2"), fql("2*3"), fql("2*4")));

        Query q = fql("${obj}", Map.of("obj", obj));
        QuerySuccess<List<Integer>> res = c.query(q, listOf(Integer.class));

        assertEquals(List.of(4,6,8), res.getData());
    }

    @Test
    public void query_listWithNestedEmbeddedQueries() {
        var inner = QueryArr.of(List.of(fql("2*2"), fql("2*3"), fql("2*4")));
        var obj = QueryArr.of(List.of(inner));
        Query q = fql("${obj}", Map.of("obj", obj));
        var res = c.query(q);

        assertEquals(List.of(List.of(4,6,8)), res.getData());
    }
}
