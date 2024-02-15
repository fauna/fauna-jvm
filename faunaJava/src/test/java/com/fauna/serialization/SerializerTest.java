package com.fauna.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fauna.common.types.Module;
import com.fauna.mapping.MappingContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SerializerTest {

    public static String serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            FaunaGenerator faunaWriter = new FaunaGenerator(stream)) {
            MappingContext ctx = new MappingContext();
            Serializer.serialize(ctx, faunaWriter, obj);
            faunaWriter.flush();
            return new String(stream.toByteArray());
        }
    }

    @Test
    public void serializeValues() throws IOException {
        Instant dt = Instant.parse("2024-01-23T13:33:10.300Z");

        HashMap<String, Object> tests = new HashMap<>();
        tests.put("\"hello\"", "hello");
        tests.put("true", true);
        tests.put("false", false);
        tests.put("null", null);
        tests.put("{\"@date\":\"2023-12-13\"}", LocalDate.of(2023, 12, 13));
        tests.put("{\"@double\":\"1.2\"}", 1.2d);
        tests.put("{\"@double\":\"1.340000033378601\"}", 1.34f);
        tests.put("{\"@int\":\"1\"}", Byte.parseByte("1"));
        tests.put("{\"@int\":\"2\"}", Byte.parseByte("2"));
        tests.put("{\"@int\":\"40\"}", Short.parseShort("40"));
        tests.put("{\"@int\":\"41\"}", Short.parseShort("41"));
        tests.put("{\"@int\":\"42\"}", 42);
        tests.put("{\"@long\":\"43\"}", 43L);
        tests.put("{\"@mod\":\"module\"}", new Module("module"));
        tests.put("{\"@time\":\"2024-01-23T13:33:10.300Z\"}", dt);

        for (Map.Entry<String, Object> entry : tests.entrySet()) {
            String expected = entry.getKey();
            Object test = entry.getValue();
            String result = serialize(test);
            assertEquals(expected, result);
        }
    }

}