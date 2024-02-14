package com.fauna.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fauna.common.types.Module;
import com.fauna.exception.SerializationException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;


public class DeserializerTest {

    public static <T> T deserialize(String str,
        Function<SerializationContext, IDeserializer<T>> deserFunc)
        throws IOException {
        FaunaParser reader = new FaunaParser(str);
        return deserialize(reader, deserFunc);
    }

    public static <T> T deserializeNullable(String str, Class<T> targetType)
        throws IOException {
        return deserialize(str, ctx -> Deserializer.generateNullable(ctx, targetType));
    }

    private static <T> T deserialize(FaunaParser reader,
        Function<SerializationContext, IDeserializer<T>> deserFunc)
        throws IOException {
        reader.read();
        SerializationContext context = new SerializationContext();
        IDeserializer<T> deser = deserFunc.apply(context);
        T obj = deser.deserialize(context, reader);

        if (reader.read()) {
            throw new SerializationException(
                "Token stream is not exhausted but should be: " + reader.getCurrentTokenType());
        }

        return obj;
    }

    @Test
    public void deserializeNullable() throws IOException {
        String result = deserializeNullable("null", String.class);
        assertNull(result);
    }

    @Test
    public void deserializeValues() throws IOException {
        Map<String, Object> tests = new HashMap<>();
        tests.put("\"hello\"", "hello");
        tests.put("{\"@int\":\"42\"}", 42);
        tests.put("{\"@long\":\"42\"}", 42L);
        tests.put("{\"@double\": \"1.2\"}", 1.2d);
        tests.put("{\"@date\": \"2023-12-03\"}", LocalDate.of(2023, 12, 3));
        tests.put("{\"@time\": \"2023-12-03T05:52:10.000001-09:00\"}",
            Instant.parse("2023-12-03T05:52:10.000001-09:00"));
        tests.put("{\"@time\": \"2024-01-23T13:33:10.300Z\"}",
            Instant.parse("2024-01-23T13:33:10.300Z"));
        tests.put("{\"@mod\": \"MyModule\"}", new Module("MyModule"));
        tests.put("true", true);
        tests.put("false", false);
        tests.put("null", null);

        for (Map.Entry<String, Object> entry : tests.entrySet()) {
            Object result = deserialize(entry.getKey(), ctx -> Deserializer.DYNAMIC);
            assertEquals(entry.getValue(), result);
        }
        for (Map.Entry<String, Object> entry : tests.entrySet()) {
            if (entry.getValue() != null) {
                Object result = deserialize(entry.getKey(),
                    ctx -> Deserializer.generate(ctx, entry.getValue().getClass()));
                assertEquals(entry.getValue(), result);
            }

        }
    }
}