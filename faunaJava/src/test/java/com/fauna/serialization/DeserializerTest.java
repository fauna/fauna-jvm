package com.fauna.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fauna.common.types.Module;
import com.fauna.exception.SerializationException;
import com.google.common.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static <T> T deserialize(InputStream inputStream,
        Function<SerializationContext, IDeserializer<T>> deserFunc)
        throws IOException {
        FaunaParser reader = new FaunaParser(inputStream);
        return deserialize(reader, deserFunc);
    }

    public static <T> T deserializeNullable(String str, TypeToken<T> targetType)
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
    public void testDeserializeInt() throws IOException {
        int result = deserialize("{\"@int\":\"42\"}",
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertEquals(42, result);
    }

    @Test
    public void testDeserializeString() throws IOException {
        String result = deserialize("\"hello\"",
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertEquals("hello", result);
    }

    @Test
    public void deserializeNullable() throws IOException {
        String result = deserializeNullable("null", new TypeToken<>() {
        });
        assertNull(result);
    }

    @Test
    public void deserializeDate() throws IOException {
        LocalDate result = deserialize("{\"@date\": \"2023-12-03\"}",
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertEquals(LocalDate.of(2023, 12, 3), result);
    }

    @Test
    public void deserializeTime() throws IOException {
        Instant result = deserialize("{\"@time\": \"2024-01-23T13:33:10.300Z\"}",
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        Instant instant = Instant.parse("2024-01-23T13:33:10.300Z");
        assertEquals(instant, result);
    }

    @Test
    public void deserializeTimeNoUTC() throws IOException {
        Instant result = deserialize("{\"@time\": \"2023-12-03T05:52:10.000001-09:00\"}",
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        Instant instant = Instant.parse("2023-12-03T05:52:10.000001-09:00");
        assertEquals(instant, result);
    }

    @Test
    public void testDeserializeDouble() throws IOException {
        Double result = deserialize("{\"@double\":\"1.23\"}",
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertEquals(1.23d, result);
    }

    @Test
    public void testDeserializeLong() throws IOException {
        Long result = deserialize("{\"@long\":\"123\"}",
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertEquals(123l, result);
    }

    @Test
    public void testDeserializeBooleanTrue() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("true".getBytes());
        Boolean result = deserialize(inputStream,
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertTrue(result);
    }

    @Test
    public void testDeserializeBooleanFalse() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("false".getBytes());
        Boolean result = deserialize(inputStream,
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertFalse(result);
    }

    @Test
    public void testDeserializeLModule() throws IOException {
        Module result = deserialize("{\"@mod\": \"MyModule\"}",
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertEquals(new Module("MyModule"), result);
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
        tests.put("true", true);
        tests.put("false", false);
        tests.put("null", null);

        for (Map.Entry<String, Object> entry : tests.entrySet()) {
            Object result = deserialize(entry.getKey(), ctx -> Deserializer.DYNAMIC);
            assertEquals(entry.getValue(), result);
        }
    }
}