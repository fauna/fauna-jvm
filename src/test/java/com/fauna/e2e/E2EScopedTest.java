package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.query.builder.Query;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EScopedTest {
    public static final FaunaClient baseClient = Fauna.local();
    public static final FaunaClient scopedClient =
            Fauna.scoped(baseClient, "People");

    @BeforeAll
    public static void setup() {
        Fixtures.PeopleDatabase(baseClient);
    }

    @Test
    public void query_sync() {
        var q = Query.fql("42");
        var res = scopedClient.query(q);
        var exp = 42;
        assertEquals(exp, res.getData());
    }
}
