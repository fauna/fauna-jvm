package com.fauna.client;

import com.fauna.mapping.MappingContext;
import com.fauna.serialization.FaunaGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class IQueryFragmentExtensions {

    public static String serialize(IQueryFragment fragment, MappingContext ctx) throws IOException {
        try (ByteArrayOutputStream ms = new ByteArrayOutputStream();
            FaunaGenerator fw = new FaunaGenerator(ms)) {
            fragment.serialize(ctx, fw);
            fw.flush();
            return new String(ms.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
