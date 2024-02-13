package com.fauna.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fauna.common.types.Document;
import com.fauna.common.types.Module;
import com.fauna.common.types.NamedDocument;
import com.fauna.exception.SerializationException;
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

    @Test
    public void testDeserializeDouble() throws IOException {
        Double result = deserialize("{\"@double\":\"1.23\"}",
            ctx -> Deserializer.generate(ctx, Double.class));
        assertEquals(1.23d, result);
    }

    @Test
    public void testDeserializeLong() throws IOException {
        Long result = deserialize("{\"@long\":\"123\"}",
            ctx -> Deserializer.generate(ctx, Long.class));
        assertEquals(123l, result);
    }

    @Test
    public void testDeserializeBooleanTrue() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("true".getBytes());
        Boolean result = deserialize(inputStream,
            ctx -> Deserializer.generate(ctx, Boolean.class));
        assertTrue(result);
    }

    @Test
    public void testDeserializeBooleanFalse() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("false".getBytes());
        Boolean result = deserialize(inputStream,
            ctx -> Deserializer.generate(ctx, Boolean.class));
        assertFalse(result);
    }

    @Test
    public void testDeserializeLModule() throws IOException {
        Module result = deserialize("{\"@mod\": \"MyModule\"}",
            ctx -> Deserializer.generate(ctx, Module.class));
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

    @Test
    public void deserializeDocument() throws IOException {
        String given = "{\n" +
            "    \"@doc\": {\n" +
            "        \"id\": \"123\",\n" +
            "        \"coll\": {\"@mod\": \"MyColl\"},\n" +
            "        \"ts\": {\"@time\": \"2023-12-15T01:01:01.0010010Z\"},\n" +
            "        \"name\": \"name_value\"\n" +
            "    }\n" +
            "}";

        Object actual = deserialize(given, ctx -> Deserializer.DYNAMIC);

        assertTrue(actual instanceof Document);
        Document typed = (Document) actual;
        assertEquals("123", typed.getId());
        assertEquals(new Module("MyColl"), typed.getCollection());
        assertEquals(Instant.parse("2023-12-15T01:01:01.0010010Z"), typed.getTs());
        assertEquals("name_value", typed.get("name"));
    }

    @Test
    public void deserializeDocumentWithType() throws IOException {
        String given = "{\n" +
            "    \"@doc\": {\n" +
            "        \"id\": \"123\",\n" +
            "        \"coll\": {\"@mod\": \"MyColl\"},\n" +
            "        \"ts\": {\"@time\": \"2023-12-15T01:01:01.0010010Z\"},\n" +
            "        \"name\": \"name_value\"\n" +
            "    }\n" +
            "}";

        Document actual = deserialize(given,
            ctx -> Deserializer.generate(ctx, Document.class));
        assertEquals("123", actual.getId());
        assertEquals(new Module("MyColl"), actual.getCollection());
        assertEquals(Instant.parse("2023-12-15T01:01:01.0010010Z"), actual.getTs());
        assertEquals("name_value", actual.get("name"));
    }

    @Test
    public void deserializeNamedDocument() throws IOException {
        String given = "{\n" +
            "    \"@doc\":{\n" +
            "        \"name\":\"DocName\",\n" +
            "        \"coll\":{\"@mod\":\"MyColl\"},\n" +
            "        \"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\n" +
            "        \"user_field\":\"user_value\"\n" +
            "    }\n" +
            "}";

        Object actual = deserialize(given, ctx -> Deserializer.DYNAMIC);
        assertTrue(actual instanceof NamedDocument);
        NamedDocument typed = (NamedDocument) actual;
        assertEquals("DocName", typed.getName());
        assertEquals(new Module("MyColl"), typed.getCollection());
        assertEquals(Instant.parse("2023-12-15T01:01:01.0010010Z"), typed.getTs());
        assertEquals("user_value", typed.get("user_field"));
    }

    @Test
    public void deserializeNamedDocumentWithType() throws IOException {
        String given = "{\n" +
            "    \"@doc\":{\n" +
            "        \"name\":\"DocName\",\n" +
            "        \"coll\":{\"@mod\":\"MyColl\"},\n" +
            "        \"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\n" +
            "        \"user_field\":\"user_value\"\n" +
            "    }\n" +
            "}";

        NamedDocument actual = deserialize(given,
            ctx -> Deserializer.generate(ctx, NamedDocument.class));
        assertEquals("DocName", actual.getName());
        assertEquals(new Module("MyColl"), actual.getCollection());
        assertEquals(Instant.parse("2023-12-15T01:01:01.0010010Z"), actual.getTs());
        assertEquals("user_value", actual.get("user_field"));
    }

}