package com.fauna.client;

import com.fauna.mapping.MappingContext;
import com.fauna.serialization.FaunaGenerator;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QueryExpr extends Query implements IQueryFragment {

    private final List<IQueryFragment> unwrap;

    public QueryExpr(List<IQueryFragment> fragments) {
        unwrap = Collections.unmodifiableList(fragments);
    }

    public QueryExpr(IQueryFragment... fragments) {
        this(List.of(fragments));
    }

    public List<IQueryFragment> getUnwrap() {
        return unwrap;
    }

    @Override
    public void serialize(MappingContext ctx, FaunaGenerator writer) throws IOException {
        writer.writeStartObject();
        writer.writeFieldName("fql");
        writer.writeStartArray();
        for (IQueryFragment fragment : unwrap) {
            fragment.serialize(ctx, writer);
        }
        writer.writeEndArray();
        writer.writeEndObject();
    }


    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof QueryExpr && isEqual((QueryExpr) o);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(unwrap);
    }

    @Override
    public String toString() {
        return "QueryExpr(" + unwrap + ")";
    }

    private boolean isEqual(QueryExpr o) {
        return Objects.equals(unwrap, o.unwrap);
    }

    public static boolean equals(QueryExpr left, QueryExpr right) {
        return Objects.equals(left, right);
    }

    public static boolean notEquals(QueryExpr left, QueryExpr right) {
        return !Objects.equals(left, right);
    }
}