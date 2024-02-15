package com.fauna.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fauna.common.types.Document;
import com.fauna.common.types.DocumentRef;
import com.fauna.common.types.Module;
import com.fauna.common.types.NamedDocument;
import com.fauna.common.types.NamedDocumentRef;
import com.fauna.common.types.NullDocumentRef;
import com.fauna.common.types.NullNamedDocumentRef;
import com.fauna.common.types.Page;
import com.fauna.exception.SerializationException;
import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    @Test
    public void deserializeDocumentRef() throws IOException {
        String given = "{\n" +
            "    \"@ref\":{\n" +
            "        \"id\":\"123\",\n" +
            "        \"coll\":{\n" +
            "            \"@mod\":\"MyColl\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

        DocumentRef actual = deserialize(given,
            ctx -> Deserializer.generate(ctx, DocumentRef.class));

        assertEquals("123", actual.getId());
        assertEquals(new Module("MyColl"), actual.getCollection());
    }

    @Test
    public void deserializeNullRef() throws IOException {
        String given = "{\n" +
            "    \"@ref\":{\n" +
            "        \"id\":\"123\",\n" +
            "        \"coll\":{\n" +
            "            \"@mod\":\"MyColl\"\n" +
            "        },\n" +
            "        \"exists\":false,\n" +
            "        \"cause\":\"not found\"\n" +
            "    }\n" +
            "}";

        NullDocumentRef actual = deserialize(given,
            ctx -> Deserializer.generate(ctx, NullDocumentRef.class));

        assertEquals("123", actual.getId());
        assertEquals(new Module("MyColl"), actual.getCollection());
        assertEquals("not found", actual.getCause());
    }

    @Test
    public void deserializeNamedRef() throws IOException {
        String given = "{\n" +
            "    \"@ref\":{\n" +
            "        \"name\":\"RefName\",\n" +
            "        \"coll\":{\n" +
            "            \"@mod\":\"MyColl\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

        NamedDocumentRef actual = deserialize(given,
            ctx -> Deserializer.generate(ctx, NamedDocumentRef.class));

        assertEquals("RefName", actual.getName());
        assertEquals(new Module("MyColl"), actual.getCollection());
    }

    @Test
    public void deserializeNullNamedRef() throws IOException {
        String given = "{\n" +
            "    \"@ref\":{\n" +
            "        \"name\":\"RefName\",\n" +
            "        \"coll\":{\n" +
            "            \"@mod\":\"MyColl\"\n" +
            "        },\n" +
            "        \"exists\":false,\n" +
            "        \"cause\":\"not found\"\n" +
            "    }\n" +
            "}";

        NullNamedDocumentRef actual = deserialize(given,
            ctx -> Deserializer.generate(ctx, NullNamedDocumentRef.class));

        assertEquals("RefName", actual.getName());
        assertEquals(new Module("MyColl"), actual.getCollection());
        assertEquals("not found", actual.getCause());
    }

    @Test
    public void deserializeObject() throws IOException {
        String given = "{\n" +
            "    \"aString\": \"foo\",\n" +
            "    \"anObject\": { \"baz\": \"luhrmann\" },\n" +
            "    \"anInt\": { \"@int\": \"2147483647\" },\n" +
            "    \"aLong\":{ \"@long\": \"9223372036854775807\" },\n" +
            "    \"aDouble\":{ \"@double\": \"3.14159\" },\n" +
            "    \"aDate\":{ \"@date\": \"2023-12-03\" },\n" +
            "    \"aTime\":{ \"@time\": \"2023-12-03T14:52:10.001001Z\" },\n" +
            "    \"true\": true,\n" +
            "    \"false\": false,\n" +
            "    \"null\": null\n" +
            "}";

        Map<String, Object> inner = new HashMap<>();
        inner.put("baz", "luhrmann");

        Map<String, Object> expected = new HashMap<>();
        expected.put("aString", "foo");
        expected.put("anObject", inner);
        expected.put("anInt", 2147483647);
        expected.put("aLong", 9223372036854775807L);
        expected.put("aDouble", 3.14159d);
        expected.put("aDate", LocalDate.parse("2023-12-03"));
        expected.put("aTime", Instant.parse("2023-12-03T14:52:10.001001Z"));
        expected.put("true", true);
        expected.put("false", false);
        expected.put("null", null);

        Object result = deserialize(given, ctx -> Deserializer.DYNAMIC);

        assertEquals(expected, result);

    }

    @Test
    public void deserializeEscapedObject() throws IOException {
        String given = "{\n" +
            "    \"@object\": {\n" +
            "        \"@int\": \"notanint\",\n" +
            "        \"anInt\": { \"@int\": \"123\" },\n" +
            "        \"@object\": \"notanobject\",\n" +
            "        \"anEscapedObject\": { \"@object\": { \"@long\": \"notalong\" } }\n" +
            "    }\n" +
            "}";

        Map<String, Object> inner = new HashMap<>();
        inner.put("@long", "notalong");

        Map<String, Object> expected = new HashMap<>();
        expected.put("@int", "notanint");
        expected.put("anInt", 123);
        expected.put("@object", "notanobject");
        expected.put("anEscapedObject", inner);

        Object result = deserialize(given, ctx -> Deserializer.DYNAMIC);
        assertEquals(expected, result);
    }

    @Test
    public void deserializeIntoGenericDictionary() throws IOException {
        String given = "{\n" +
            "    \"k1\": { \"@int\": \"1\" },\n" +
            "    \"k2\": { \"@int\": \"2\" },\n" +
            "    \"k3\": { \"@int\": \"3\" }\n" +
            "}";

        Map<String, Integer> expected = new HashMap<>();
        expected.put("k1", 1);
        expected.put("k2", 2);
        expected.put("k3", 3);

        Map<String, Integer> result = deserialize(given,
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertEquals(expected, result);
    }

    @Test
    public void deserializeIntoList() throws IOException {
        String given = "[\"item1\",\"item2\"]";
        List<Object> expected = Arrays.asList("item1", "item2");
        Object result = deserialize(given, ctx -> Deserializer.DYNAMIC);
        assertEquals(expected, result);
    }

    @Test
    public void deserializeIntoGenericListWithPrimitive() throws IOException {
        String given = "[\"item1\",\"item2\"]";
        List<String> expected = Arrays.asList("item1", "item2");
        Object result = deserialize(given, ctx -> Deserializer.DYNAMIC);
        assertEquals(expected, result);
    }

    @Test
    public void DeserializeIntoList() throws IOException {
        String given = "[\"item1\",\"item2\"]";
        List<Object> expected = new ArrayList<>();
        expected.add("item1");
        expected.add("item2");
        Object result = deserialize(given, ctx -> Deserializer.DYNAMIC);
        assertEquals(expected, result);
    }

    @Test
    public void DeserializeIntoGenericListWithPrimitive() throws IOException {
        String given = "[\"item1\",\"item2\"]";
        List<String> expected = new ArrayList<>();
        expected.add("item1");
        expected.add("item2");
        List<String> result = deserialize(given,
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));
        assertEquals(expected, result);
    }

    @Test
    public void DeserializeIntoPageWithPrimitive() throws IOException {
        String given = "{\n" +
            "  \"@set\": {\n" +
            "    \"after\": \"next_page_cursor\",\n" +
            "    \"data\": [\n" +
            "      {\"@int\":\"1\"},\n" +
            "      {\"@int\":\"2\"},\n" +
            "      {\"@int\":\"3\"}\n" +
            "    ]\n" +
            "  }\n" +
            "}";

        Page<Integer> expected = new Page<>(Arrays.asList(1, 2, 3), "next_page_cursor");
        Page<Integer> result = deserialize(given,
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));

        assertNotNull(result);
        assertEquals(expected.data(), result.data());
        assertEquals(expected.after(), result.after());
    }

    @Test
    public void deserializeIntoPageWithObject() throws IOException {
        String given = "{\n" +
            "  \"after\": \"next_page_cursor\",\n" +
            "  \"data\": [\n" +
            "    {\"@int\":\"1\"},\n" +
            "    {\"@int\":\"2\"},\n" +
            "    {\"@int\":\"3\"}\n" +
            "  ]\n" +
            "}";

        Page<Integer> expected = new Page<>(Arrays.asList(1, 2, 3), "next_page_cursor");
        Page<Integer> result = deserialize(given,
            ctx -> Deserializer.generate(ctx, new TypeToken<>() {
            }));

        assertNotNull(result);
        assertEquals(expected.data(), result.data());
        assertEquals(expected.after(), result.after());
    }

}