package com.fauna.codec;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fauna.codec.UTF8FaunaParser;
import com.fauna.enums.FaunaTokenType;
import com.fauna.types.Module;
import com.fauna.exception.ClientException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;


class UTF8FaunaParserTest {

    @Test
    public void testGetValueAsString() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("\"hello\"".getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.STRING, "hello")
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsByteArray() throws IOException {
        String s = "{\"@bytes\": \"RmF1bmE=\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
                Map.entry(FaunaTokenType.BYTES, "Fauna".getBytes())
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsInt() throws IOException {
        String s = "{\"@int\": \"123\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.INT, 123)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsIntFail() throws IOException {
        String invalidJson = "{\"@int\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        UTF8FaunaParser invalidReader = new UTF8FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.INT, "abc")
        );

        Exception ex = assertThrows(ClientException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as Integer", ex.getMessage());
    }

    @Test
    public void testUnexpectedEndDuringAdvance() throws IOException {

        String json = "{\"@int\": \"123\"";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        Exception ex = assertThrows(ClientException.class,
            () -> new UTF8FaunaParser(inputStream));

        assertEquals("Failed to advance underlying JSON reader.", ex.getMessage());
    }

    @Test
    public void testGetValueAsBooleanTrue() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("true".getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.TRUE, true)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsBooleanFalse() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("false".getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.FALSE, false)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsLocalDate() throws IOException {
        String s = "{\"@date\":\"2024-01-23\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DATE, LocalDate.of(2024, 1, 23))
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsLocalDateFail() throws IOException {
        String invalidJson = "{\"@date\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        UTF8FaunaParser invalidReader = new UTF8FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DATE, "abc")
        );

        Exception ex = assertThrows(ClientException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as LocalDate", ex.getMessage());
    }

    @Test
    public void testGetValueAsTime() throws IOException {
        String s = "{\"@time\":\"2024-01-23T13:33:10.300Z\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        Instant instant = Instant.parse("2024-01-23T13:33:10.300Z");

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.TIME, instant)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsTimeFail() throws IOException {
        String invalidJson = "{\"@time\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        UTF8FaunaParser invalidReader = new UTF8FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.TIME, "abc")
        );

        Exception ex = assertThrows(ClientException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as LocalDateTime", ex.getMessage());
    }

    @Test
    public void testeGetValueAsTimeNonUTC() throws IOException {
        String s = "{\"@time\":\"2023-12-03T05:52:10.000001-09:00\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        Instant instant = Instant.parse("2023-12-03T05:52:10.000001-09:00");

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.TIME, instant)
        );

        assertReader(reader, expectedTokens);

    }

    @Test
    public void testGetValueAsDouble() throws IOException {
        String s = "{\"@double\": \"1.23\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DOUBLE, 1.23D)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsDoubleFail() throws IOException {
        String invalidJson = "{\"@double\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        UTF8FaunaParser invalidReader = new UTF8FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DOUBLE, "abc")
        );

        Exception ex = assertThrows(ClientException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as Double", ex.getMessage());
    }

    @Test
    public void testGetValueAsLong() throws IOException {
        String s = "{\"@long\": \"123\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.LONG, 123L)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsLongFail() throws IOException {
        String invalidJson = "{\"@long\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        UTF8FaunaParser invalidReader = new UTF8FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.LONG, "abc")
        );

        Exception ex = assertThrows(ClientException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as Long", ex.getMessage());
    }

    @Test
    public void testGetValueAsModule() throws IOException {
        String s = "{\"@mod\": \"MyModule\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.MODULE, new Module("MyModule"))
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void readArrayWithEmptyObject() throws IOException {
        String s = "[{}]";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        UTF8FaunaParser reader = new UTF8FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_ARRAY, null),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_OBJECT, null),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_OBJECT, null),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_ARRAY, null)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testReadEscapedObject() throws IOException {
        String s = "{\n" +
            "    \"@object\": {\n" +
            "        \"@int\": \"notanint\",\n" +
            "        \"anInt\": { \"@int\": \"123\" },\n" +
            "        \"@object\": \"notanobject\",\n" +
            "        \"anEscapedObject\": { \"@object\": { \"@long\": \"notalong\" } }\n" +
            "    }\n" +
            "}";
        UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(s.getBytes()));

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_OBJECT, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "@int"),
            Map.entry(FaunaTokenType.STRING, "notanint"),
            Map.entry(FaunaTokenType.FIELD_NAME, "anInt"),
            Map.entry(FaunaTokenType.INT, 123),
            Map.entry(FaunaTokenType.FIELD_NAME, "@object"),
            Map.entry(FaunaTokenType.STRING, "notanobject"),
            Map.entry(FaunaTokenType.FIELD_NAME, "anEscapedObject"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_OBJECT, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "@long"),
            Map.entry(FaunaTokenType.STRING, "notalong"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_OBJECT, null),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_OBJECT, null)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testReadDocumentTokens() throws IOException {
        String s = "{\n" +
            "    \"@doc\": {\n" +
            "        \"id\": \"123\",\n" +
            "        \"coll\": { \"@mod\": \"Coll\" },\n" +
            "        \"ts\": { \"@time\": \"2023-12-03T16:07:23.111012Z\" },\n" +
            "        \"data\": { \"foo\": \"bar\" }\n" +
            "    }\n" +
            "}";
        UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(s.getBytes()));

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_DOCUMENT, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "id"),
            Map.entry(FaunaTokenType.STRING, "123"),
            Map.entry(FaunaTokenType.FIELD_NAME, "coll"),
            Map.entry(FaunaTokenType.MODULE, new Module("Coll")),
            Map.entry(FaunaTokenType.FIELD_NAME, "ts"),
            Map.entry(FaunaTokenType.TIME, Instant.parse("2023-12-03T16:07:23.111012Z")),
            Map.entry(FaunaTokenType.FIELD_NAME, "data"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_OBJECT, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "foo"),
            Map.entry(FaunaTokenType.STRING, "bar"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_OBJECT, null),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_DOCUMENT, null)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testReadSet() throws IOException {
        String s = "{\n" +
            "    \"@set\": {\n" +
            "        \"data\": [{\"@int\": \"99\"}],\n" +
            "        \"after\": \"afterme\"\n" +
            "    }\n" +
            "}";
        UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(s.getBytes()));

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_PAGE, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "data"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_ARRAY, null),
            Map.entry(FaunaTokenType.INT, 99),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_ARRAY, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "after"),
            Map.entry(FaunaTokenType.STRING, "afterme"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_PAGE, null)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testReadRef() throws IOException {
        String s = "{\"@ref\": {\"id\": \"123\", \"coll\": {\"@mod\": \"Col\"}}}";

        UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(s.getBytes()));

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_REF, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "id"),
            Map.entry(FaunaTokenType.STRING, "123"),
            Map.entry(FaunaTokenType.FIELD_NAME, "coll"),
            Map.entry(FaunaTokenType.MODULE, new Module("Col")),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_REF, null)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testReadObjectTokens() throws IOException {
        String s = "{\n" +
            "    \"aString\": \"foo\",\n" +
            "    \"anObject\": { \"baz\": \"luhrmann\" },\n" +
            "    \"anInt\": { \"@int\": \"2147483647\" },\n" +
            "    \"aLong\":{ \"@long\": \"9223372036854775807\" },\n" +
            "    \"aDouble\":{ \"@double\": \"3.14159\" },\n" +
            "    \"aDecimal\":{ \"@double\": \"0.1\" },\n" +
            "    \"aDate\":{ \"@date\": \"2023-12-03\" },\n" +
            "    \"aTime\":{ \"@time\": \"2023-12-03T14:52:10.001001Z\" },\n" +
            "    \"anEscapedObject\": { \"@object\": { \"@int\": \"escaped\" } },\n" +
            "    \"anArray\": [],\n" +
            "    \"true\": true,\n" +
            "    \"false\": false,\n" +
            "    \"null\": null\n" +
            "}";
        UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(s.getBytes()));

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_OBJECT, null),

            Map.entry(FaunaTokenType.FIELD_NAME, "aString"),
            Map.entry(FaunaTokenType.STRING, "foo"),

            Map.entry(FaunaTokenType.FIELD_NAME, "anObject"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_OBJECT, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "baz"),
            Map.entry(FaunaTokenType.STRING, "luhrmann"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_OBJECT, null),

            Map.entry(FaunaTokenType.FIELD_NAME, "anInt"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.INT, 2147483647),

            Map.entry(FaunaTokenType.FIELD_NAME, "aLong"),
            Map.entry(FaunaTokenType.LONG, 9223372036854775807L),

            Map.entry(FaunaTokenType.FIELD_NAME, "aDouble"),
            Map.entry(FaunaTokenType.DOUBLE, 3.14159d),

            Map.entry(FaunaTokenType.FIELD_NAME, "aDecimal"),
            Map.entry(FaunaTokenType.DOUBLE, 0.1d),

            Map.entry(FaunaTokenType.FIELD_NAME, "aDate"),
            Map.entry(FaunaTokenType.DATE, LocalDate.of(2023, 12, 3)),

            Map.entry(FaunaTokenType.FIELD_NAME, "aTime"),
            Map.entry(FaunaTokenType.TIME, Instant.parse("2023-12-03T14:52:10.001001Z")),

            Map.entry(FaunaTokenType.FIELD_NAME, "anEscapedObject"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_OBJECT, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "@int"),
            Map.entry(FaunaTokenType.STRING, "escaped"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_OBJECT, null),

            Map.entry(FaunaTokenType.FIELD_NAME, "anArray"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_ARRAY, null),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_ARRAY, null),

            Map.entry(FaunaTokenType.FIELD_NAME, "true"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.TRUE, true),

            Map.entry(FaunaTokenType.FIELD_NAME, "false"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.FALSE, false),

            Map.entry(FaunaTokenType.FIELD_NAME, "null"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.NULL, null),

            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_OBJECT, null)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testReadArray() throws IOException {
        String s = "[\n" +
            "    \"foo\",\n" +
            "    { \"baz\": \"luhrmann\" },\n" +
            "    { \"@int\": \"2147483647\" },\n" +
            "    { \"@long\": \"9223372036854775807\" },\n" +
            "    { \"@double\": \"3.14159\" },\n" +
            "    { \"@double\": \"0.1\" },\n" +
            "    { \"@date\": \"2023-12-03\" },\n" +
            "    { \"@time\": \"2023-12-03T14:52:10.001001Z\" },\n" +
            "    { \"@object\": { \"@int\": \"escaped\" } },\n" +
            "    [],\n" +
            "    true,\n" +
            "    false,\n" +
            "    null\n" +
            "]";
        UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(s.getBytes()));

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_ARRAY, null),

            Map.entry(FaunaTokenType.STRING, "foo"),

            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_OBJECT, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "baz"),
            Map.entry(FaunaTokenType.STRING, "luhrmann"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_OBJECT, null),

            Map.entry(FaunaTokenType.INT, 2147483647),

            Map.entry(FaunaTokenType.LONG, 9223372036854775807L),

            Map.entry(FaunaTokenType.DOUBLE, 3.14159d),

            Map.entry(FaunaTokenType.DOUBLE, 0.1d),

            Map.entry(FaunaTokenType.DATE, LocalDate.of(2023, 12, 3)),

            Map.entry(FaunaTokenType.TIME, Instant.parse("2023-12-03T14:52:10.001001Z")),

            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_OBJECT, null),
            Map.entry(FaunaTokenType.FIELD_NAME, "@int"),
            Map.entry(FaunaTokenType.STRING, "escaped"),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_OBJECT, null),

            new AbstractMap.SimpleEntry<>(FaunaTokenType.START_ARRAY, null),
            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_ARRAY, null),

            Map.entry(FaunaTokenType.TRUE, true),

            Map.entry(FaunaTokenType.FALSE, false),

            new AbstractMap.SimpleEntry<>(FaunaTokenType.NULL, null),

            new AbstractMap.SimpleEntry<>(FaunaTokenType.END_ARRAY, null)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void throwsOnMalformedJson() {
        String s = "{";
        assertThrows(ClientException.class, () -> {
            UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(s.getBytes()));
            reader.read();
            reader.read();
        }, "Failed to advance underlying JSON reader.");
    }

    @Test
    public void skipValues() throws IOException {
        List<String> tests = List.of(
            "{\"k1\": {}, \"k2\": {}}",
            "[\"k1\",[],{}]",
            "{\"@ref\": {}}",
            "{\"@doc\": {}}",
            "{\"@set\": {}}",
            "{\"@object\":{}}"
        );

        for (String test : tests) {
            UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(test.getBytes()));
            reader.skip();
            assertFalse(reader.read());
        }
    }

    @Test
    public void skipNestedEscapedObject() throws IOException {
        String test = "{\"@object\": {\"inner\": {\"@object\": {\"foo\": \"bar\"}}, \"k2\": {}}}";
        UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(test.getBytes()));
        assertEquals(FaunaTokenType.START_OBJECT, reader.getCurrentTokenType());
        reader.read(); // inner
        assertEquals(FaunaTokenType.FIELD_NAME, reader.getCurrentTokenType());
        reader.read(); // {"@object":{
        assertEquals(FaunaTokenType.START_OBJECT, reader.getCurrentTokenType());
        reader.skip(); // "foo": "bar"}}
        assertEquals(FaunaTokenType.END_OBJECT, reader.getCurrentTokenType());
        reader.read();
        assertEquals(FaunaTokenType.FIELD_NAME, reader.getCurrentTokenType());
        assertEquals("k2", reader.getValueAsString());
    }

    @Test
    public void skipNestedObject() throws IOException {
        String test = "{\"k\":{\"inner\":{}},\"k2\":{}}";
        UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(test.getBytes()));
        assertEquals(FaunaTokenType.START_OBJECT, reader.getCurrentTokenType());
        reader.read(); // k
        assertEquals(FaunaTokenType.FIELD_NAME, reader.getCurrentTokenType());
        reader.read(); // {
        assertEquals(FaunaTokenType.START_OBJECT, reader.getCurrentTokenType());
        reader.skip(); // "inner":{}}
        assertEquals(FaunaTokenType.END_OBJECT, reader.getCurrentTokenType());
        reader.read();
        assertEquals(FaunaTokenType.FIELD_NAME, reader.getCurrentTokenType());
        assertEquals("k2", reader.getValueAsString());
    }

    @Test
    public void skipNestedArrays() throws IOException {
        String test = "{\"k\":[\"1\",\"2\"],\"k2\":{}}";
        UTF8FaunaParser reader = new UTF8FaunaParser(new ByteArrayInputStream(test.getBytes()));
        assertEquals(FaunaTokenType.START_OBJECT, reader.getCurrentTokenType());
        reader.read(); // k
        assertEquals(FaunaTokenType.FIELD_NAME, reader.getCurrentTokenType());
        reader.read(); // [
        assertEquals(FaunaTokenType.START_ARRAY, reader.getCurrentTokenType());
        reader.skip(); // "1","2"]
        assertEquals(FaunaTokenType.END_ARRAY, reader.getCurrentTokenType());
        reader.read();
        assertEquals(FaunaTokenType.FIELD_NAME, reader.getCurrentTokenType());
        assertEquals("k2", reader.getValueAsString());
    }

    private static void assertReader(UTF8FaunaParser reader,
                                     List<Map.Entry<FaunaTokenType, Object>> tokens) throws IOException {
        for (Map.Entry<FaunaTokenType, Object> entry : tokens) {
            assertNotNull(entry.getKey());
            assertNotNull(reader.getCurrentTokenType());
            assertEquals(entry.getKey(), reader.getCurrentTokenType());

            switch (entry.getKey()) {
                case FIELD_NAME:
                case STRING:
                    assertEquals(entry.getValue(), reader.getValueAsString());
                    break;
                case BYTES:
                    var ar1 = (byte[])entry.getValue();
                    var ar2 = reader.getValueAsByteArray();
                    assertArrayEquals(ar1, ar2);
                    break;
                case INT:
                    assertEquals(entry.getValue(), reader.getValueAsInt());
                    break;
                case TRUE:
                case FALSE:
                    assertEquals(entry.getValue(), reader.getValueAsBoolean());
                    break;
                case DATE:
                    assertEquals(entry.getValue(), reader.getValueAsLocalDate());
                    break;
                case TIME:
                    assertEquals(entry.getValue(), reader.getValueAsTime());
                    break;
                case DOUBLE:
                    assertEquals(entry.getValue(), reader.getValueAsDouble());
                    break;
                case LONG:
                    assertEquals(entry.getValue(), reader.getValueAsLong());
                    break;
                case MODULE:
                    assertEquals(entry.getValue(), reader.getValueAsModule());
                    break;
                default:
                    assertNull(entry.getValue());
                    break;
            }

            reader.read();
        }

        assertFalse(reader.read());
    }

}