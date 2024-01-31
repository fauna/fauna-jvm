package com.fauna.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fauna.common.enums.FaunaTokenType;
import com.fauna.common.types.Module;
import com.fauna.exception.SerializationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;


class FaunaParserTest {

    @Test
    public void testGetValueAsString() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("\"hello\"".getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.STRING, "hello")
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsInt() throws IOException {
        String s = "{\"@int\": \"123\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.INT, 123)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsIntFail() throws IOException {
        String invalidJson = "{\"@int\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.INT, "abc")
        );

        Exception ex = assertThrows(SerializationException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as Integer", ex.getMessage());
    }

    @Test
    public void testUnexpectedEndDuringAdvance() throws IOException {

        String json = "{\"@int\": \"123\"";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        Exception ex = assertThrows(SerializationException.class,
            () -> reader.read());

        assertEquals("Failed to advance underlying JSON reader.", ex.getMessage());
    }

    @Test
    public void testGetValueAsBooleanTrue() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("true".getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.TRUE, true)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsBooleanFalse() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("false".getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.FALSE, false)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsLocalDate() throws IOException {
        String s = "{\"@date\":\"2024-01-23\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DATE, LocalDate.of(2024, 1, 23))
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsLocalDateFail() throws IOException {
        String invalidJson = "{\"@date\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DATE, "abc")
        );

        Exception ex = assertThrows(SerializationException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as LocalDate", ex.getMessage());
    }

    @Test
    public void testGetValueAsTime() throws IOException {
        String s = "{\"@time\":\"2024-01-23T13:33:10.300Z\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

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
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.TIME, "abc")
        );

        Exception ex = assertThrows(SerializationException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as LocalDateTime", ex.getMessage());
    }

    @Test
    public void testeGetValueAsTimeNonUTC() throws IOException {
        String s = "{\"@time\":\"2023-12-03T05:52:10.000001-09:00\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

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
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DOUBLE, 1.23D)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsDoubleFail() throws IOException {
        String invalidJson = "{\"@double\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DOUBLE, "abc")
        );

        Exception ex = assertThrows(SerializationException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as Double", ex.getMessage());
    }

    @Test
    public void testGetValueAsLong() throws IOException {
        String s = "{\"@long\": \"123\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.LONG, 123L)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsLongFail() throws IOException {
        String invalidJson = "{\"@long\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.LONG, "abc")
        );

        Exception ex = assertThrows(SerializationException.class,
            () -> assertReader(invalidReader, expectedTokens));

        assertEquals("Error getting the current token as Long", ex.getMessage());
    }

    @Test
    public void testGetValueAsModule() throws IOException {
        String s = "{\"@mod\": \"MyModule\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.MODULE, new Module("MyModule"))
        );

        assertReader(reader, expectedTokens, false);
    }

    private static void assertReader(FaunaParser reader,
        List<Map.Entry<FaunaTokenType, Object>> tokens) throws IOException {
        for (Map.Entry<FaunaTokenType, Object> entry : tokens) {
            reader.read();
            assertNotNull(entry.getKey());
            assertNotNull(reader.getCurrentTokenType());
            assertEquals(entry.getKey(), reader.getCurrentTokenType());

            switch (entry.getKey()) {
                case FIELD_NAME:
                case STRING:
                    assertEquals(entry.getValue(), reader.getValueAsString());
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
                    assertNull(entry.getValue() == null);
                    break;
            }
        }

        assertFalse(reader.read());
    }

}