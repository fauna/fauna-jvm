package com.fauna.query.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

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

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testQueryBuilderStrings() {
        Query actual = fql("let x = 11", Collections.emptyMap());
        Fragment[] expected = new Fragment[]{new LiteralFragment("let x = 11")};
        assertArrayEquals(expected, actual.getFql());
    }

    @Test
    public void testQueryBuilderStrings_WithNullValue() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("n", null);

        Query actual = fql("let x = ${n}", args);
        assertArrayEquals(new Fragment[] {new LiteralFragment("let x = "), new ValueFragment(null)}, actual.getFql());
    }

    @Test
    public void testQueryBuilderInterpolatedStrings() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("n1", 5);
        Query actual = fql("let age = ${n1}\n\"Alice is #{age} years old.\"", variables);
        Fragment[] expected = new Fragment[] {
                new LiteralFragment("let age = "),
                new ValueFragment(5),
                new LiteralFragment("\n\"Alice is #{age} years old.\"")};
        assertArrayEquals(expected, actual.getFql());
    }

    @Test
    public void testQueryBuilderValues() {
        Map<String, Object> user = Map.of(
                "name", "Dino",
                "age", 0,
                "birthdate", LocalDate.of(2023, 2, 24));
        Query actual = fql("let x = ${my_var}", Map.of("my_var", user));
        Fragment[] expected = new Fragment[]{new LiteralFragment("let x = "), new ValueFragment(user)};
        assertArrayEquals(expected, actual.getFql());
    }

    @Test
    public void testQueryBuilderSubQueries() {
        Map<String, Object> user = Map.of(
                "name", "Dino",
                "age", 0,
                "birthdate", LocalDate.of(2023, 2, 24));

        Query inner = fql("let x = ${my_var}", Map.of("my_var", user));
        Query actual = fql("${inner}\nx { name }", Map.of("inner", inner));
        Fragment[] expected = new Fragment[]{new ValueFragment(inner), new LiteralFragment("\nx { name }")};
        assertArrayEquals(expected, actual.getFql());
    }

    @Test
    public void testOverloadedFqlBuildingMethods() {
        // Test that the four different fql(...) methods produce equivalent results.
        Query explicit_vars = fql("let age = 5\n\"Alice is #{age} years old.\"", Map.of());
        Query implicit_vars = fql("let age = 5\n\"Alice is #{age} years old.\"", null);
        Query no_vars = fql("let age = 5\n\"Alice is #{age} years old.\"");
        assertArrayEquals(explicit_vars.getFql(), implicit_vars.getFql());
        assertArrayEquals(no_vars.getFql(), implicit_vars.getFql());
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
        assertArrayEquals(q1.getFql(), q2.getFql());
        assertArrayEquals(q1.getFql(), q3.getFql());
    }

    @Test
    public void testQuerySerialization() throws JsonProcessingException {
        Query q1 = fql("let one = ${a}", Map.of("a", 0xf));
        assertEquals("{\"fql\":[\"let one = \",{\"value\":15}]}",
                mapper.writeValueAsString(q1));
    }
}