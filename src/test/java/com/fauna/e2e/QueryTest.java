package com.fauna.e2e;


import com.fauna.beans.Person;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryTest {

    @Test
    public void Repro() throws ExecutionException, InterruptedException, IOException {
        var given = new Person("Alice", "Wonderland", 'N', 0);
        var c = LocalFauna.get();
        var q = fql("${given}", new HashMap<>() {{
                put("given", given);
            }}
        );

        Person actual =  c.query(q).to(Person.class);
        assertEquals(given.getFirstName(), actual.getFirstName());
        assertEquals(given.getMiddleInitial(), actual.getMiddleInitial());
        assertEquals(given.getFirstName(), actual.getFirstName());
        assertEquals(given.getFirstName(), actual.getFirstName());
    }
}
