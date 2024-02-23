package com.fauna.client;

import com.fauna.mapping.MappingContext;
import com.fauna.serialization.FaunaGenerator;
import java.io.IOException;

public final class QueryLiteral implements IQueryFragment {

    private final String unwrap;

    public QueryLiteral(String v) {
        if (v == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }
        unwrap = v;
    }

    public String getUnwrap() {
        return unwrap;
    }

    @Override
    public String toString() {
        return "QueryLiteral(" + unwrap + ")";
    }

    @Override
    public void serialize(MappingContext ctx, FaunaGenerator writer) throws IOException {
        writer.writeStringValue(unwrap);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        QueryLiteral that = (QueryLiteral) other;
        return unwrap.equals(that.unwrap);
    }

    @Override
    public int hashCode() {
        return unwrap.hashCode();
    }

    public static boolean equals(QueryLiteral left, QueryLiteral right) {
        return left.equals(right);
    }

    public static boolean notEquals(QueryLiteral left, QueryLiteral right) {
        return !left.equals(right);
    }
}