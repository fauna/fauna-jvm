package com.fauna.e2e;


import com.fauna.beans.Person;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryTest {

    @Test
    public void Repro() throws ExecutionException, InterruptedException {
        var given = new Person("Alice", "Wonderland", 'N', 0);
        var c = LocalFauna.get();
        var q = fql("${given}", new HashMap<>() {{
                put("given", given);
            }}
        );

        Future<Person> r = c.query(q, Person.class);

        var actual = r.get();
        assertEquals(given.getFirstName(), actual.getFirstName());
        assertEquals(given.getMiddleInitial(), actual.getMiddleInitial());
        assertEquals(given.getFirstName(), actual.getFirstName());
        assertEquals(given.getFirstName(), actual.getFirstName());
    }
}
