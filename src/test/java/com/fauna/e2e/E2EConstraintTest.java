package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.exception.ConstraintFailureException;
import com.fauna.response.ConstraintFailure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.fauna.query.builder.Query.fql;
import static java.time.LocalTime.now;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EConstraintTest {
    public static final FaunaClient client = Fauna.local();

    @BeforeAll
    public static void setup() {
        Fixtures.ProductCollection(client);
    }

    @Test
    public void checkConstraintFailure() throws IOException {
        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class,
                () -> client.query(fql("Product.create({name: ${name}, quantity: -1})", Map.of("name", now().toString()))));

        ConstraintFailure actual = exc.getConstraintFailures().get(0);
        assertEquals("Document failed check constraint `posQuantity`", actual.getMessage());
        assertTrue(actual.getName().isEmpty());
        assertEquals(0, actual.getPaths().size());
    }

    @Test
    public void uniqueConstraintFailure() throws IOException {
        client.query(fql("Product.create({name: 'cheese', quantity: 1})"));

        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class,
                () -> client.query(fql("Product.create({name: 'cheese', quantity: 2})")));

        ConstraintFailure actual = exc.getConstraintFailures().get(0);
        assertEquals("Failed unique constraint", actual.getMessage());
        assertTrue(actual.getName().isEmpty());

        var paths = actual.getPaths();
        assertEquals(1, paths.size());
        assertEquals(List.of("name"), paths.get(0));
    }
}
