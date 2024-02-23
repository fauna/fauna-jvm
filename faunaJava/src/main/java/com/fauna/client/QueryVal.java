package com.fauna.client;

import com.fauna.mapping.MappingContext;
import com.fauna.serialization.FaunaGenerator;
import com.fauna.serialization.Serializer;
import java.io.IOException;
import java.util.Objects;

public final class QueryVal extends Query implements IQueryFragment {

    private final Object unwrap;

    public QueryVal(Object v) {
        unwrap = v;
    }

    public Object getUnwrap() {
        return unwrap;
    }

    @Override
    public void serialize(MappingContext ctx, FaunaGenerator writer) throws IOException {
        writer.writeStartObject();
        writer.writeFieldName("value");
        Serializer.serialize(ctx, writer, unwrap);
        writer.writeEndObject();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof QueryVal && isEqual((QueryVal) o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unwrap);
    }

    @Override
    public String toString() {
        return "QueryVal(" + unwrap + ")";
    }

    private boolean isEqual(QueryVal o) {
        return Objects.equals(unwrap, o.unwrap);
    }

    public static boolean equals(QueryVal left, QueryVal right) {
        return Objects.equals(left, right);
    }

    public static boolean notEquals(QueryVal left, QueryVal right) {
        return !Objects.equals(left, right);
    }
}
