package com.fauna.query.builder;

import com.fauna.query.template.FaunaTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a Fauna query that is constructed from fragments.
 * This class allows the building of queries from literal and variable parts.
 */
public class Query {

    private final List<Fragment> fragments;

    /**
     * Constructs an empty Query instance.
     */
    public Query() {
        this.fragments = new ArrayList<>();
    }

    /**
     * Adds a fragment to the query.
     *
     * @param fragment the Fragment to add to the query.
     */
    void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }

    /**
     * Creates a Query instance from a string template and arguments.
     * The template string can contain literals and variable placeholders.
     *
     * @param query the string template of the query.
     * @param args  the arguments to replace the variable placeholders in the query.
     * @return a Query instance representing the complete query.
     * @throws IllegalArgumentException if a template variable does not have a corresponding entry in the provided args.
     */
    public static Query fql(String query, Map<String, Object> args) throws IllegalArgumentException {
        Query faunaQuery = new Query();
        FaunaTemplate template = new FaunaTemplate(query);

        for (FaunaTemplate.TemplatePart part : template) {
            switch (part.getType()) {
                case LITERAL: {
                    faunaQuery.addFragment(new LiteralFragment(part.getPart()));
                    break;
                }
                case VARIABLE: {
                    if (!args.containsKey(part.getPart())) {
                        throw new IllegalArgumentException("Template variable `" + part.getPart() + "` not found in provided args");
                    }
                    faunaQuery.addFragment(new ValueFragment(args.get(part.getPart())));
                    break;
                }
            }
        }
        return faunaQuery;
    }

    /**
     * Retrieves the list of fragments that make up this query.
     *
     * @return a list of Fragments.
     */
    List<Fragment> getFragments() {
        return fragments;
    }

}
