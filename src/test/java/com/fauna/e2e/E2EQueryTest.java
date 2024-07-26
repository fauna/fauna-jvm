
package com.fauna.e2e;

import com.fauna.client.FaunaClient;
import com.fauna.e2e.beans.Author;
import com.fauna.query.builder.Query;
import com.fauna.serialization.generic.Parameterized;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EQueryTest {
    public static final FaunaClient c = com.fauna.e2e.LocalFauna.get();

    @BeforeAll
    public static void setup() {
        Fixtures.PersonCollection(c);
    }

    @Test
    public void query_arrayOfInt() {
        var q = Query.fql("[1,2,3]");
        var res = c.query(q, Parameterized.listOf(int.class));
        var exp = new ArrayList<>() {{
            add(1);
            add(2);
            add(3);
        }};

        assertEquals(exp, res.getData());
    }

    @Test
    public void query_arrayOfPerson() {
        var q = Query.fql("Author.all().toArray()");

        var res = c.query(q, Parameterized.listOf(Author.class));

        assertEquals(2, res.getData().size());

        var elem = res.getData().get(0);
        assertEquals("Alice", elem.getFirstName());
    }

    @Test
    public void query_mapOfPerson() {
        var p = new Author("Alice", "Wonderland", "N", 65);
        var q = Query.fql("{key: ${person}}", new HashMap<>() {{
            put("person", p);
        }});

        var qs = c.query(q, Parameterized.mapOf(Author.class));
        var actual = qs.getData();

        assertEquals(1, actual.size());
        assertEquals(p.getFirstName(), actual.get("key").getFirstName());
    }

    @Test
    public void query_pageOfPerson() {
        var q = Query.fql("Author.all()");

        var qs = c.query(q, Parameterized.pageOf(Author.class));
        var actual = qs.getData().data();

        assertEquals(2, actual.size());
        assertEquals("Alice", actual.get(0).getFirstName());
    }
}
