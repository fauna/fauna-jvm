package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.e2e.beans.Author;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.*;
import static com.fauna.serialization.generic.Parameterized.listOf;
import static com.fauna.serialization.generic.Parameterized.mapOf;
import static com.fauna.serialization.generic.Parameterized.pageOf;
import static com.fauna.serialization.generic.Parameterized.optionalOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EQueryTest {
    public static final FaunaClient c = Fauna.local();

    @BeforeAll
    public static void setup() {
        Fixtures.PersonCollection(c);
    }

    @Test
    public void query_sync() {
        Query q = fql("42");
        var res = c.query(q);
        var exp = 42;
        assertEquals(exp, res.getData());
    }

    @Test
    public void query_syncWithClass() {
        var q = fql("42");
        var res = c.query(q, int.class);
        var exp = 42;
        assertEquals(exp, res.getData());
    }


    @Test
    public void query_syncWithParameterized() {
        var q = fql("[42]");
        var res = c.query(q, listOf(int.class));
        var exp = List.of(42);
        assertEquals(exp, res.getData());
    }

    @Test
    public void query_syncWithClassAndOptions() {
        var q = fql("42");
        var res = c.query(q, int.class, QueryOptions.builder().build());
        var exp = 42;
        assertEquals(exp, res.getData());
    }

    @Test
    public void query_syncWithParameterizedAndOptions() {
        var q = fql("[42]");
        var res = c.query(q, listOf(int.class), QueryOptions.builder().build());
        var exp = List.of(42);
        assertEquals(exp, res.getData());
    }

    @Test
    public void query_async() throws ExecutionException, InterruptedException {
        var q = fql("42");
        var res = c.asyncQuery(q).get();
        var exp = 42;
        assertEquals(exp, res.getData());
    }

    @Test
    public void query_asyncWithClass() throws ExecutionException, InterruptedException {
        var q = fql("42");
        var res = c.asyncQuery(q, int.class).get();
        var exp = 42;
        assertEquals(exp, res.getData());
    }

    @Test
    public void query_asyncWithParameterized() throws ExecutionException, InterruptedException {
        var q = fql("[42]");
        var res = c.asyncQuery(q, listOf(int.class)).get();
        var exp = List.of(42);
        assertEquals(exp, res.getData());
    }

    @Test
    public void query_asyncWithClassAndOptions() throws ExecutionException, InterruptedException {
        var q = fql("42");
        var res = c.asyncQuery(q, int.class, QueryOptions.builder().build()).get();
        var exp = 42;
        assertEquals(exp, res.getData());
    }

    @Test
    public void query_asyncWithParameterizedAndOptions() throws ExecutionException, InterruptedException {
        var q = fql("[42]");
        var res = c.asyncQuery(q, listOf(int.class), QueryOptions.builder().build()).get();
        var exp = List.of(42);
        assertEquals(exp, res.getData());
    }

    @Test
    public void query_arrayOfPerson() {
        var q = fql("Author.all().toArray()");

        var res = c.query(q, listOf(Author.class));

        assertEquals(2, res.getData().size());

        var elem = res.getData().get(0);
        assertEquals("Alice", elem.getFirstName());
    }

    @Test
    public void query_mapOfPerson() {
        var p = new Author("Alice", "Wonderland", "N", 65);
        var q = fql("{key: ${person}}", new HashMap<>() {{
            put("person", p);
        }});

        var qs = c.query(q, mapOf(Author.class));
        var actual = qs.getData();

        assertEquals(1, actual.size());
        assertEquals(p.getFirstName(), actual.get("key").getFirstName());
    }

    @Test
    public void query_pageOfPerson() {
        var q = fql("Author.all()");

        var qs = c.query(q, pageOf(Author.class));
        var actual = qs.getData().data();

        assertEquals(2, actual.size());
        assertEquals("Alice", actual.get(0).getFirstName());
    }

    @Test
    public void query_optionalNull() {
        var empty = Optional.empty();
        var q = fql("${empty}", new HashMap<>(){{
            put("empty", empty);
        }});

        var qs = c.query(q, optionalOf(int.class));
        var actual = qs.getData();

        assertTrue(actual.isEmpty());
    }

    @Test
    public void query_optionalNotNull() {
        var val = Optional.of(42);
        var q = fql("${val}", new HashMap<>(){{
            put("val", val);
        }});

        var qs = c.query(q, optionalOf(int.class));
        var actual = qs.getData();

        assertTrue(actual.isPresent());
        assertEquals(42, actual.get());
    }
}
