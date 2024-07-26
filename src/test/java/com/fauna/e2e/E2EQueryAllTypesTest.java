package com.fauna.e2e;

import com.fauna.client.FaunaClient;
import com.fauna.codec.ListCodec;
import com.fauna.query.builder.Query;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EQueryAllTypesTest {
    public static final FaunaClient c = LocalFauna.get();

    @Test
    public void query_int() {
        var q = Query.fql("1");
        var res = c.query(q, int.class);
        assertEquals(1, res.getData());
    }

    @Test
    public void query_array() {
        var q = Query.fql("[1,2,3]");
        var codec = new ListCodec<>(int.class);
        var res = c.query(q, codec);
        var exp = new ArrayList<>() {{
            add(1);
            add(2);
            add(3);
        }};

        assertEquals(exp, res.getData());
    }
}
