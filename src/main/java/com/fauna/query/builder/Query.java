package com.fauna.query.builder;

import com.fauna.query.template.FaunaTemplate;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.StreamSupport;


/**
 * Represents a Fauna query that is constructed from fragments.
 * This class allows the building of queries from literal and variable parts.
 */
public class Query implements Serializable {

    private final Fragment[] fql;

    /**
     * Construct a Query from the given template String and args.
     * @param query A Fauna Query Language (FQL) v10 template string.
     * @param args  A map of variable names -> values.
     */
    public Query(String query, Map<String, Object> args) throws IllegalArgumentException {
        Spliterator<FaunaTemplate.TemplatePart> iter = new FaunaTemplate(query).spliterator();
        this.fql = StreamSupport.stream(iter, true).map(
                part -> {
                    Map<String, Object> foo = Objects.requireNonNullElse(args, Map.of());
                    return part.toFragment(foo);
                }).toArray(Fragment[]::new);
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
        return new Query(query, args);
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
     * Retrieves the list of fragments that make up this query.
     *
     * @return a list of Fragments.
     * @throws IllegalArgumentException if a template variable does not have a corresponding entry in the provided args.
     */
    public Fragment[] getFql() {
        return this.fql;
    }
}
