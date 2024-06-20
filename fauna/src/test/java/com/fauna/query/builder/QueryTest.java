package com.fauna.query.builder;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryTest {


    @Test
    public void testQueryBuilderStrings() {
        Query actual = fql("let x = 11", Collections.emptyMap());
        Fragment[] expected = new Fragment[]{new LiteralFragment("let x = 11")};
        assertArrayEquals(expected, actual.getFragments());
    }

    @Test
    public void testQueryBuilderStrings_WithNullValue() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("n", null);

        Query actual = fql("let x = ${n}", args);
        assertArrayEquals(new Fragment[] {new LiteralFragment("let x = "), new ValueFragment(null)}, actual.getFragments());
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
        assertArrayEquals(expected, actual.getFragments());
    }

    @Test
    public void testQueryBuilderValues() {
        Map<String, Object> user = Map.of(
                "name", "Dino",
                "age", 0,
                "birthdate", LocalDate.of(2023, 2, 24));
        Query actual = fql("let x = ${my_var}", Map.of("my_var", user));
        Fragment[] expected = new Fragment[]{new LiteralFragment("let x = "), new ValueFragment(user)};
        assertArrayEquals(expected, actual.getFragments());
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
        assertArrayEquals(expected, actual.getFragments());
    }

    @Test
    public void testOverloadedFqlBuildingMethods() {
        // Test that the four different fql(...) methods produce equivalent results.
        Map<String, Object> vars = Map.of("n1", 5);
        Query explicit_vars = fql(List.of("let age = ${n1}", "\"Alice is #{age} years old.\""), vars);
        Query implicit_vars = fql("let age = ${n1}\n\"Alice is #{age} years old.\"", vars);
        assertArrayEquals(explicit_vars.getFragments(), implicit_vars.getFragments());

        Query explicit_novars = fql("let age = 5", "\"Alice is #{age} years old.\"");
        Query implicit_novars = fql("let age = 5\n\"Alice is #{age} years old.\"");
        assertArrayEquals(explicit_novars.getFragments(), implicit_novars.getFragments());
        assertNotEquals(explicit_vars, explicit_novars);
    }

    @Test
    public void testQueryWithMissingArgs() {
        IllegalArgumentException first = assertThrows(IllegalArgumentException.class,
                () -> fql("let first = ${first}"));
        assertEquals("No args provided for Template variable first.", first.getMessage());
        IllegalArgumentException second = assertThrows(IllegalArgumentException.class,
                () -> fql("let first = ${first}\n", "let second = ${second}"));
        assertEquals("No args provided for Template variable first.", first.getMessage());
    }

    @Test
    public void testQueryUsingMessageFormat() {
        String email = "alice@home.com";
        Query q1 = fql(MessageFormat.format("Users.firstWhere(.email == {0})", email));
        Query q2 = fql(String.format("Users.firstWhere(.email == %s)", email));
        Query q3 = fql(new StringBuilder().append("Users.firstWhere(.email == ").append(email).append(")").toString());
        assertArrayEquals(q1.getFragments(), q2.getFragments());
        assertArrayEquals(q1.getFragments(), q3.getFragments());
    }

    @Test
    public void testMultiLineQueries() {
        Query q1 = fql("let one = 1\nlet two = 2");
        Query q2 = fql("let one = 1", "let two = 2");
        assertArrayEquals(q1.getFragments(), q2.getFragments());
    }

    @Test
    public void testMultiLineQueriesWithArgs() {
        Map<String, Object> args = Map.of("one", 1);
        Query q1 = fql("let one = ${one}\nlet two = 2", args);
        Query q2 = fql(List.of("let one = ${one}", "let two = 2"), args);
        Query q3 = fql("let one = ${one}\rlet two = 2", args);

        assertArrayEquals(q1.getFragments(), q2.getFragments());
        assertEquals(q1.getFragments()[0].toString(), q3.getFragments()[0].toString());
        assertEquals(q1.getFragments()[1].toString(), q3.getFragments()[1].toString());
        assertNotEquals(q2.getFragments()[2], q3.getFragments()[2]);
    }

}