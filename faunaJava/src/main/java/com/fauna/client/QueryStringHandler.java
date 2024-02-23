package com.fauna.client;

import java.util.ArrayList;
import java.util.List;

public class QueryStringHandler {

    private List<IQueryFragment> fragments;

    public QueryStringHandler(int literalLength, int formattedCount) {
        fragments = new ArrayList<>();
    }

    public void appendLiteral(String value) {
        fragments.add(new QueryLiteral(value));
    }

    public void appendFormatted(Object value) {
        if (value instanceof QueryExpr) {
            fragments.add((QueryExpr) value);
        } else {
            fragments.add(new QueryVal(value));
        }
    }

    public Query result() {
        return new QueryExpr(fragments);
    }
}
