package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;
import com.fauna.exception.AbortException;
import com.fauna.exception.ConstraintFailureException;
import com.fauna.response.ConstraintFailure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import static com.fauna.query.builder.Query.fql;
import static java.time.LocalTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EErrorHandlingTest {
    public static FaunaClient client;

    @BeforeAll
    public static void setup() throws IOException {
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new SimpleFormatter());
        client = Fauna.client(FaunaConfig.builder().logHandler(handler).endpoint(FaunaConfig.FaunaEndpoint.LOCAL).secret("secret").build());

        Fixtures.ProductCollection(client);
    }

    @Test
    public void checkConstraintFailure() throws IOException {
        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class,
                () -> client.query(fql("Product.create({name: ${name}, quantity: -1})", Map.of("name", now().toString()))));

        ConstraintFailure actual = exc.getConstraintFailures()[0];
        assertEquals("Document failed check constraint `posQuantity`", actual.getMessage());
        assertTrue(actual.getName().isEmpty());
        assertTrue(actual.getPaths().isEmpty());
    }

    @Test
    public void uniqueConstraintFailure() throws IOException {
        client.query(fql("Product.create({name: 'cheese', quantity: 1})"));

        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class,
                () -> client.query(fql("Product.create({name: 'cheese', quantity: 2})"), String.class));

        ConstraintFailure actual = exc.getConstraintFailures()[0];
        assertEquals("Failed unique constraint", actual.getMessage());
        assertTrue(actual.getName().isEmpty());

        ConstraintFailure.PathElement[][] paths = actual.getPaths().get();
        assertEquals(1, paths.length);
        assertEquals(List.of("name"), actual.getPathStrings().orElseThrow());
    }

    @Test
    public void constraintFailureWithInteger() {
        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class, () -> client.query(
                fql("Collection.create({name: \"Foo\", constraints: [{unique: [\"$$$\"] }]})")));

        ConstraintFailure actual = exc.getConstraintFailures()[0];
        ConstraintFailure expected = ConstraintFailure.builder()
                .message("Value `$` is not a valid FQL path expression.")
                .path(ConstraintFailure.createPath("constraints", 0, "unique"))
                .build();
        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected, actual);
    }

    @Test
    public void testAbortAPI() throws IOException {
        Instant bigBang = Instant.parse("2019-12-31T23:59:59.999Z");
        AbortException exc = assertThrows(AbortException.class, () -> client.query(fql("abort(${bigBang})", Map.of("bigBang", bigBang))));
        assertEquals(999000000, exc.getAbort(Instant.class).getNano());
        assertEquals(Instant.class, exc.getAbort().getClass());
        assertEquals(999000000, ((Instant) exc.getAbort()).getNano());
    }
}
