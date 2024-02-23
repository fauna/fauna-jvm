package com.fauna.client;

import com.fauna.mapping.MappingContext;
import com.fauna.serialization.FaunaGenerator;
import java.io.IOException;

public interface IQueryFragment {

    void serialize(MappingContext ctx, FaunaGenerator writer) throws IOException;
}

