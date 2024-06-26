package com.fauna.query.builder;

import com.fauna.query.template.FaunaTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a Fauna query that is constructed from fragments.
 * This class allows the building of queries from literal and variable parts.
 */
public class Query {

    private final Fragment[] fragments;

    /**
     * Constructs a Query instance from the list of Fragments.
     * @param fragments
     */
    public Query(@Nonnull Fragment ... fragments) {
        this.fragments = fragments;
    }


    /**
     * Creates a Query instance from a String and arguments.
     * The template strings can contain literals and variable placeholders.
     *
     * @param query the string template of the query.
     * @param args  the arguments to replace the variable placeholders in the query.
     * @return a Query instance representing the complete query.
     * @throws IllegalArgumentException if a template variable does not have a corresponding entry in the provided args.
     */
    public static Query fql(@Nonnull String query,
                            @Nullable Map<String, Object> args) throws IllegalArgumentException {
        return new Query(StreamSupport.stream(
                new FaunaTemplate(query).spliterator(), true).map(
                        part -> part.toFragment(args)).toArray(Fragment[]::new));
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
    public static Query fql(@Nonnull List<String> literals,
                            @Nullable Map<String, Object> args) throws IllegalArgumentException {
        return fql(String.join("\n", literals), args);
    }

    /**
     * Creates a Query instance from a String.
     * The template string can only contain literals.
     *
     * @param query the string template of the query.
     * @return a Query instance representing the complete query.
     * @throws IllegalArgumentException if a template variable does not have a corresponding entry in the provided args.
     */
    public static Query fql(@Nonnull String query) throws IllegalArgumentException {
        return fql(query, null);
    }

    /**
     *
     */
    public static Query fql(@Nonnull String ... literals) throws IllegalArgumentException {
        return fql(List.of(literals), null);

    }

    /**
     * Retrieves the list of fragments that make up this query.
     *
     * @return a list of Fragments.
     */
    Fragment[] getFragments() {
        return fragments;
    }

}
