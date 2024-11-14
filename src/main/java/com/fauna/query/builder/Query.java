package com.fauna.query.builder;

import com.fauna.query.template.FaunaTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.StreamSupport;


/**
 * Represents a Fauna query that is constructed from multiple query fragments. This class enables the creation of
 * queries from both literal strings and variable placeholders.
 */
@SuppressWarnings("rawtypes")
public class Query extends QueryFragment<QueryFragment[]> {

    private final QueryFragment[] fql;

    /**
     * Constructs a Query instance based on the given template string and variable arguments.
     *
     * @param query A Fauna Query Language (FQL) v10 template string containing both literals and variable placeholders.
     *              Placeholders should follow the syntax defined by {@link FaunaTemplate}.
     * @param args  A map of variable names to their corresponding values. This map provides the values that are
     *              substituted for placeholders in the template string.
     * @throws IllegalArgumentException if any placeholder in the template string lacks a matching entry in
     *                                  {@code args}.
     */
    public Query(final String query, final Map<String, Object> args)
            throws IllegalArgumentException {
        Spliterator<FaunaTemplate.TemplatePart> iter =
                new FaunaTemplate(query).spliterator();
        this.fql = StreamSupport.stream(iter, true).map(
                part -> {
                    Map<String, Object> foo =
                            Objects.requireNonNullElse(args, Map.of());
                    return part.toFragment(foo);
                }).toArray(QueryFragment[]::new);
    }

    /**
     * Creates a Query instance based on a template string and a set of arguments. The template string can contain both
     * literals and variable placeholders, allowing for dynamic query construction.
     *
     * @param query A Fauna Query Language (FQL) v10 template string. It may contain variables designated by
     *              placeholders.
     * @param args  A map associating variable names with their values for substitution within the query. If
     *              {@code null}, no variables will be substituted.
     * @return a Query instance representing the constructed query.
     * @throws IllegalArgumentException if any placeholder in the template string lacks a corresponding entry in
     *                                  {@code args}.
     */
    public static Query fql(
            final String query,
            final Map<String, Object> args)
            throws IllegalArgumentException {
        return new Query(query, args);
    }

    /**
     * Creates a Query instance based solely on a template string without any arguments. The template string should
     * contain only literals since no variable values are provided.
     *
     * @param query A Fauna Query Language (FQL) v10 template string. This version of the template should contain no
     *              placeholders, as there are no arguments for substitution.
     * @return a Query instance representing the constructed query.
     * @throws IllegalArgumentException if the template contains placeholders without a matching entry in the provided
     *                                  arguments.
     */
    public static Query fql(final String query)
            throws IllegalArgumentException {
        return fql(query, null);
    }

    /**
     * Retrieves the list of fragments that compose this query, where each fragment is either a literal or a variable.
     *
     * @return an array of QueryFragment instances representing the parts of the query.
     */
    @Override
    public QueryFragment[] get() {
        return this.fql;
    }
}
