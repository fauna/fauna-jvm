package com.fauna.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fauna.common.types.Module;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SerializerTest {

    public static String serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            FaunaGenerator faunaWriter = new FaunaGenerator(stream)) {
            SerializationContext ctx = new SerializationContext();
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

    @Test
    public void serializeDictionary() throws IOException {
        Map<String, Object> test = new HashMap<>();
        test.put("answer", 42);
        test.put("foo", "bar");
        test.put("list", new ArrayList<>());
        test.put("obj", new HashMap<>());

        String actual = serialize(test);
        assertEquals("{\"answer\":{\"@int\":\"42\"},\"obj\":{},\"foo\":\"bar\",\"list\":[]}",
            actual);
    }

    @Test
    public void serializeDictionaryWithTagConflicts() throws IOException {
        Map<Map<String, Object>, String> tests = new HashMap<>();
        tests.put(new HashMap<>() {{
            put("@date", "not");
        }}, "{\"@object\":{\"@date\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@doc", "not");
        }}, "{\"@object\":{\"@doc\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@double", "not");
        }}, "{\"@object\":{\"@double\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@int", "not");
        }}, "{\"@object\":{\"@int\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@long", "not");
        }}, "{\"@object\":{\"@long\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@mod", "not");
        }}, "{\"@object\":{\"@mod\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@object", "not");
        }}, "{\"@object\":{\"@object\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@ref", "not");
        }}, "{\"@object\":{\"@ref\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@set", "not");
        }}, "{\"@object\":{\"@set\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@time", "not");
        }}, "{\"@object\":{\"@time\":\"not\"}}");

        for (Map.Entry<Map<String, Object>, String> entry : tests.entrySet()) {
            String expected = entry.getValue();
            Map<String, Object> test = entry.getKey();
            String actual = serialize(test);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void serializeList() throws IOException {
        List<Object> test = new ArrayList<>();
        test.add(42);
        test.add("foo bar");
        test.add(new ArrayList<>());
        test.add(new HashMap<>());

        String actual = serialize(test);
        assertEquals("[{\"@int\":\"42\"},\"foo bar\",[],{}]", actual);
    }

}