package com.fauna.client;

import com.fauna.mapping.MappingContext;
import com.fauna.serialization.FaunaGenerator;
import java.io.IOException;
import java.util.Objects;

public abstract class Query implements Comparable<Query> {

    public abstract void serialize(MappingContext ctx, FaunaGenerator writer) throws IOException;

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Query other = (Query) obj;
        return Objects.equals(this, other);
    }

    @Override
    public int compareTo(Query other) {
        return this.equals(other) ? 0 : -1;
    }

    public static Query fql(QueryStringHandler handler) {
        return handler.result();
    }
}
