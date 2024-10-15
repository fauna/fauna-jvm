package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.e2e.beans.Author;
import com.fauna.exception.AbortException;
import com.fauna.exception.QueryRuntimeException;
import com.fauna.exception.QueryTimeoutException;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QuerySuccess;
import com.fauna.types.NonNullDocument;
import com.fauna.types.NullDocument;
import com.fauna.types.NullableDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.fauna.codec.Generic.nullableDocumentOf;
import static com.fauna.query.builder.Query.fql;
import static com.fauna.codec.Generic.listOf;
import static com.fauna.codec.Generic.mapOf;
import static com.fauna.codec.Generic.pageOf;
import static com.fauna.codec.Generic.optionalOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void clientTransactionTsOnSuccess() {
        FaunaClient client = Fauna.local();
        assertTrue(client.getLastTransactionTs().isEmpty());
        client.query(fql("42"));
        long y2k = Instant.parse("1999-12-31T23:59:59.99Z").getEpochSecond() * 1_000_000;
        assertTrue(client.getLastTransactionTs().orElseThrow() > y2k);
    }

    @Test
    public void clientTransactionTsOnFailure() {
        FaunaClient client = Fauna.local();
        assertTrue(client.getLastTransactionTs().isEmpty());
        assertThrows(QueryRuntimeException.class, () -> client.query(fql("NonExistantCollection.all()")));
        long y2k = Instant.parse("1999-12-31T23:59:59.99Z").getEpochSecond() * 1_000_000;
        assertTrue(client.getLastTransactionTs().orElseThrow() > y2k);
    }

    @Test
    public void queryTimeout() {
        QueryOptions opts = QueryOptions.builder().timeout(Duration.ofMillis(1)).build();
        QueryTimeoutException exc = assertThrows(QueryTimeoutException.class,
                () -> c.query(fql("Author.byId('9090090')"), listOf(Author.class), opts));
        assertTrue(exc.getMessage().contains("Client set aggressive deadline"));
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
    public void query_arrayOfPersonIncoming() {
        var q = fql("Author.all().toArray()");

        var res = c.query(q, listOf(Author.class));

        assertEquals(2, res.getData().size());

        var elem = res.getData().get(0);
        assertEquals("Alice", elem.getFirstName());
    }

    @Test
    public void query_arrayOfPersonOutgoing() {
        var q = fql("${var}", Map.of("var", List.of(new Author("alice","smith","w", 42))));

        var res = c.query(q);

        List<Map<String, Object>> elem = (List<Map<String, Object>>) res.getData();
        assertEquals("alice", elem.get(0).get("firstName"));
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
        var actual = qs.getData().getData();

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

    @Test
    public void query_nullableOf() {
        var q = fql("Author.byId('9090090')");

        var qs = c.query(q, nullableDocumentOf(Author.class));
        NullableDocument<Author> actual = qs.getData();
        assertInstanceOf(NullDocument.class, actual);
        assertEquals("not found", ((NullDocument<Author>)actual).getCause());
    }

    @Test
    public void query_nullableOfNotNull() {
        var q = fql("Author.all().first()");
        var qs = c.query(q, nullableDocumentOf(Author.class));
        NullableDocument<Author> actual = qs.getData();
        assertInstanceOf(NonNullDocument.class, actual);
        assertEquals("Alice", ((NonNullDocument<Author>)actual).getValue().getFirstName());
    }

    @Test
    public void query_abortNull() throws IOException {
        var q = fql("abort(null)");
        var e = assertThrows(AbortException.class, () -> c.query(q));
        assertNull(e.getAbort());
        assertNull(e.getAbort(Author.class));
    }

    @Test
    public void query_abortDynamic() throws IOException {
        var q = fql("abort(8)");
        var e = assertThrows(AbortException.class, () -> c.query(q));
        assertEquals(8, e.getAbort());
    }

    @Test
    public void query_abortClass() throws IOException {
        var q = fql("abort({firstName:\"alice\"})");
        var e = assertThrows(AbortException.class, () -> c.query(q));
        assertEquals("alice", e.getAbort(Author.class).getFirstName());
    }

    @Test
    public void query_withTags() {
        QuerySuccess<NullableDocument<Author>> success = c.query(
                fql("Author.byId('9090090')"),
                nullableDocumentOf(Author.class), QueryOptions.builder()
                        .queryTag("first", "1")
                        .queryTag("second", "2").build());
        assertEquals("1", success.getQueryTags().get("first"));
        assertEquals("2", success.getQueryTags().get("second"));
    }
}
