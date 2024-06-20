package com.fauna.query.builder;

import com.fauna.query.template.FaunaTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Represents a Fauna query that is constructed from fragments.
 * This class allows the building of queries from literal and variable parts.
 */
public class Query {

    private final String query;
    private final Map<String, Object> args;

    /**
     * Construct a Query from the given template String and args.
     * @param query A Fauna Query Language (FQL) v10 template string.
     * @param args  A map of variable names -> values.
     */
    public Query(String query, Map<String, Object> args) {
        this.query = query;
        this.args = args;
    }


    /**
     * Creates a Query instance from a String and arguments.
     * The template strings can contain literals and variable placeholders.
     *
     * @param query A Fauna Query Language (FQL) v10 template string.
     * @param args  A map of variable names -> values.
     * @return a Query instance representing the complete query.
     * @throws IllegalArgumentException if a template variable does not have a corresponding entry in the provided args.
     */
    public static Query fql(String query,
                            Map<String, Object> args) throws IllegalArgumentException {

        Query newQuery = new Query(query, args);
        newQuery.getFragments(); // Effectively validates that there is an arg for all variables.
        return newQuery;
    }

    /**
     * Creates a Query instance from a list of Strings and arguments. The strings will be joined with
     * the newline
     * The template strings can contain literals and variable placeholders.
     *
     * @param literals A list of literals. Literals will be split if they contain newline characters.
     * @param args  the arguments to replace the variable placeholders in the literals.
     * @return a Query instance representing the complete query.
     * @throws IllegalArgumentException if a template variable does not have a corresponding entry in the provided args.
     */
    public static Query fql(List<String> literals,
                            Map<String, Object> args) throws IllegalArgumentException {
        return fql(String.join("\n", literals), args);
    }

    /**
     * Creates a Query instance from a String. Without any args, the template string cannot contain variables.
     *
     * @param query the string template of the query.
     * @return a Query instance representing the complete query.
     * @throws IllegalArgumentException if a template variable does not have a corresponding entry in the provided args.
     */
    public static Query fql(String query) throws IllegalArgumentException {
        return fql(query, null);
    }

    /**
     * Creates a query instance from a series of Strings. Without any args, the template Strings cannot contain variables.
     * the template strings can only contain literals.
     */
    public static Query fql(String ... literals) throws IllegalArgumentException {
        return fql(List.of(literals), null);

    }

    /**
     * Retrieves the list of fragments that make up this query.
     *
     * @return a list of Fragments.
     * @throws IllegalArgumentException if a template variable does not have a corresponding entry in the provided args.
     */
    Fragment[] getFragments() {
        return StreamSupport.stream(
                new FaunaTemplate(query).spliterator(), true).map(
                part -> part.toFragment(args)).toArray(Fragment[]::new);
    }
}
