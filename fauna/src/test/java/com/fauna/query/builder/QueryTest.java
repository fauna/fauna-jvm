package com.fauna.query.builder;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryTest {

    private void assertFragmentsEqual(Query expected, Query actual) {
        List<Fragment> expectedFragments = expected.getFragments();
        List<Fragment> actualFragments = actual.getFragments();

        assertEquals(expectedFragments.size(), actualFragments.size(),
                "The number of fragments does not match.");

        for (int i = 0; i < expectedFragments.size(); i++) {
            assertEquals(expectedFragments.get(i), actualFragments.get(i),
                    String.format("Fragments at index %d do not match.", i));
        }
    }

    @Test
    public void testQueryBuilderStrings() {
        Query actual = fql("let x = 11", Collections.emptyMap());
        Query expected = new Query();
        expected.addFragment(new LiteralFragment("let x = 11"));
        assertFragmentsEqual(expected, actual);
    }

    @Test
    public void testQueryBuilderStrings_WithNullValue() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("n", null);
        Query actual = fql("let x = ${n}", args);
        Query expected = new Query();
        expected.addFragment(new LiteralFragment("let x = "));
        expected.addFragment(new ValueFragment(null));
        assertFragmentsEqual(expected, actual);
    }

    @Test
    public void testQueryBuilderInterpolatedStrings() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("n1", 5);
        Query actual = fql("let age = ${n1}\n\"Alice is #{age} years old.\"", variables);
        Query expected = new Query();
        expected.addFragment(new LiteralFragment("let age = "));
        expected.addFragment(new ValueFragment(5));
        expected.addFragment(new LiteralFragment("\n\"Alice is #{age} years old.\""));
        assertFragmentsEqual(expected, actual);
    }

    @Test
    public void testQueryBuilderValues() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Dino");
        user.put("age", 0);
        user.put("birthdate", LocalDate.of(2023, 2, 24));
        Map<String, Object> variables = new HashMap<>();
        variables.put("my_var", user);
        Query actual = fql("let x = ${my_var}", variables);
        Query expected = new Query();
        expected.addFragment(new LiteralFragment("let x = "));
        expected.addFragment(new ValueFragment(user));
        assertFragmentsEqual(expected, actual);
    }

    @Test
    public void testQueryBuilderSubQueries() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Dino");
        user.put("age", 0);
        user.put("birthdate", LocalDate.of(2023, 2, 24));

        Map<String, Object> innerVariables = new HashMap<>();
        innerVariables.put("my_var", user);
        Query inner = fql("let x = ${my_var}", innerVariables);

        Map<String, Object> outerVariables = new HashMap<>();
        outerVariables.put("inner", inner);
        Query actual = fql("${inner}\nx { name }", outerVariables);

        Query expected = new Query();
        expected.addFragment(new ValueFragment(inner));
        expected.addFragment(new LiteralFragment("\nx { name }"));

        assertFragmentsEqual(expected, actual);
    }

}