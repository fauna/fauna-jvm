package com.fauna.query.builder;

import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryTest {



    private String encode(Query q) throws IOException {
        var gen = new UTF8FaunaGenerator();
        DefaultCodecProvider.SINGLETON.get(Query.class).encode(gen, q);
        return gen.serialize();
    }

    @Test
    public void testQueryBuilderStrings() {
        Query actual = fql("let x = 11", Collections.emptyMap());
        QueryFragment[] expected = new QueryFragment[]{new QueryLiteral("let x = 11")};
        assertArrayEquals(expected, actual.get());
    }

    @Test
    public void testQueryBuilderStrings_WithNullValue() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("n", null);

        Query actual = fql("let x = ${n}", args);
        assertArrayEquals(new QueryFragment[] {new QueryLiteral("let x = "), new QueryVal(null)}, actual.get());
    }

    @Test
    public void testMalformedFQL() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("n", 1);

        // Bug BT-5003, this would get into an infinite loop.
        Query actual = fql("let x = $n", args);
        assertArrayEquals(new QueryFragment[] {new QueryLiteral("let x = "), new QueryLiteral("n")}, actual.get());
    }

    @Test
    public void testQueryBuilderInterpolatedStrings() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("n1", 5);
        Query actual = fql("let age = ${n1}\n\"Alice is #{age} years old.\"", variables);
        QueryFragment[] expected = new QueryFragment[] {
                new QueryLiteral("let age = "),
                new QueryVal(5),
                new QueryLiteral("\n\"Alice is #{age} years old.\"")};
        assertArrayEquals(expected, actual.get());
    }

    @Test
    public void testQueryBuilderValues() {
        Map<String, Object> user = Map.of(
                "name", "Dino",
                "age", 0,
                "birthdate", LocalDate.of(2023, 2, 24));
        Query actual = fql("let x = ${my_var}", Map.of("my_var", user));
        QueryFragment[] expected = new QueryFragment[]{new QueryLiteral("let x = "), new QueryVal(user)};
        assertArrayEquals(expected, actual.get());
    }

    @Test
    public void testQueryBuilderSubQueries() {
        Map<String, Object> user = Map.of(
                "name", "Dino",
                "age", 0,
                "birthdate", LocalDate.of(2023, 2, 24));

        Query inner = fql("let x = ${my_var}", Map.of("my_var", user));
        Query actual = fql("${inner}\nx { name }", Map.of("inner", inner));
        QueryFragment[] expected = new QueryFragment[]{new QueryVal(inner), new QueryLiteral("\nx { name }")};
        assertArrayEquals(expected, actual.get());
    }

    @Test
    public void testOverloadedFqlBuildingMethods() {
        // Test that the four different fql(...) methods produce equivalent results.
        Query explicit_vars = fql("let age = 5\n\"Alice is #{age} years old.\"", Map.of());
        Query implicit_vars = fql("let age = 5\n\"Alice is #{age} years old.\"", null);
        Query no_vars = fql("let age = 5\n\"Alice is #{age} years old.\"");
        assertArrayEquals(explicit_vars.get(), implicit_vars.get());
        assertArrayEquals(no_vars.get(), implicit_vars.get());
    }

    @Test
    public void testQueryWithMissingArgs() {
        IllegalArgumentException first = assertThrows(IllegalArgumentException.class,
                () -> fql("let first = ${first}"));
        // I haven't figured out why yet, but these error messages are sometimes:
        // "java.lang.IllegalArgumentException: message", and sometimes just "message" ??
        assertTrue(first.getMessage().contains("Template variable first not found in provided args."));
    }

    @Test
    public void testQueryUsingMessageFormat() {
        String email = "alice@home.com";
        Query q1 = fql(MessageFormat.format("Users.firstWhere(.email == {0})", email));
        Query q2 = fql(String.format("Users.firstWhere(.email == %s)", email));
        Query q3 = fql(new StringBuilder().append("Users.firstWhere(.email == ").append(email).append(")").toString());
        assertArrayEquals(q1.get(), q2.get());
        assertArrayEquals(q1.get(), q3.get());
    }

    @Test
    public void testQuerySerialization() throws IOException {
        Query q1 = fql("let one = ${a}", Map.of("a", 0xf));
        assertEquals("{\"fql\":[\"let one = \",{\"value\":{\"@int\":\"15\"}}]}",
                encode(q1));
    }
}