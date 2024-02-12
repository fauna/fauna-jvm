package com.fauna.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fauna.exception.SerializationException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.function.Function;
import org.junit.jupiter.api.Test;


public class DeserializerTest {

    private static <T> T deserialize(String str,
        Function<SerializationContext, IDeserializer<T>> deserFunc) throws IOException {
        return deserializeImpl(str, deserFunc);
    }

    public static <T> T deserializeNullable(String str, Class<T> targetType) throws IOException {
        return deserializeImpl(str, ctx -> Deserializer.generateNullable(ctx, targetType));
    }

    private static <T> T deserializeImpl(String str,
        Function<SerializationContext, IDeserializer<T>> deserFunc)
        throws IOException {
        FaunaParser reader = new FaunaParser(str);
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
            ctx -> Deserializer.generate(ctx, Integer.class));
        assertEquals(42, result);
    }

    @Test
    public void testDeserializeString() throws IOException {
        String result = deserialize("\"hello\"",
            ctx -> Deserializer.generate(ctx, String.class));
        assertEquals("hello", result);
    }

    @Test
    public void deserializeNullable() throws IOException {
        String result = deserializeNullable("null", String.class);
        assertNull(result);
    }

    @Test
    public void deserializeDate() throws IOException {
        LocalDate result = deserialize("{\"@date\": \"2023-12-03\"}",
            ctx -> Deserializer.generate(ctx, LocalDate.class));
        assertEquals(LocalDate.of(2023, 12, 3), result);
    }

    @Test
    public void deserializeTime() throws IOException {
        Instant result = deserialize("{\"@time\": \"2024-01-23T13:33:10.300Z\"}",
            ctx -> Deserializer.generate(ctx, Instant.class));
        Instant instant = Instant.parse("2024-01-23T13:33:10.300Z");
        assertEquals(instant, result);
    }

    @Test
    public void deserializeTimeNoUTC() throws IOException {
        Instant result = deserialize("{\"@time\": \"2023-12-03T05:52:10.000001-09:00\"}",
            ctx -> Deserializer.generate(ctx, Instant.class));
        Instant instant = Instant.parse("2023-12-03T05:52:10.000001-09:00");
        assertEquals(instant, result);
    }


}