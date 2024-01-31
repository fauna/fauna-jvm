package com.fauna.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fauna.common.enums.FaunaTokenType;
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

        assertReader(reader, expectedTokens, false);
    }

    @Test
    public void testGetValueAsInt() throws IOException {
        String s = "{\"@int\": \"123\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.INT, 123)
        );

        assertReader(reader, expectedTokens, false);

        String invalidJson = "{\"@int\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        expectedTokens = List.of(
            Map.entry(FaunaTokenType.INT, "abc")
        );

        assertReader(invalidReader, expectedTokens, true);
    }

    @Test
    public void testUnexpectedEndDuringAdvance() throws IOException {

        String json = "{\"@int\": \"123\"";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        assertThrows(SerializationException.class, reader::read);
    }

    @Test
    public void testGetValueAsBooleanTrue() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("true".getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.TRUE, true)
        );

        assertReader(reader, expectedTokens, false);
    }

    @Test
    public void testGetValueAsBooleanFalse() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("false".getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.FALSE, false)
        );

        assertReader(reader, expectedTokens, false);
    }

    @Test
    public void testeGetValueAsLocalDate() throws IOException {
        String s = "{\"@date\":\"2024-01-23\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DATE, LocalDate.of(2024, 01, 23))
        );

        assertReader(reader, expectedTokens, false);

        String invalidJson = "{\"@date\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        expectedTokens = List.of(
            Map.entry(FaunaTokenType.DATE, "abc")
        );

        assertReader(invalidReader, expectedTokens, true);
    }

    @Test
    public void testeGetValueAsTime() throws IOException {
        String s = "{\"@time\":\"2024-01-23T13:33:10.300Z\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        Instant instant = Instant.parse("2024-01-23T13:33:10.300Z");

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.TIME, instant)
        );

        assertReader(reader, expectedTokens, false);

        String invalidJson = "{\"@time\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        expectedTokens = List.of(
            Map.entry(FaunaTokenType.TIME, "abc")
        );

        assertReader(invalidReader, expectedTokens, true);
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

        assertReader(reader, expectedTokens, false);

    }

    @Test
    public void testGetValueAsDouble() throws IOException {
        String s = "{\"@double\": \"1.23\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DOUBLE, 1.23D)
        );

        assertReader(reader, expectedTokens, false);

        String invalidJson = "{\"@double\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        expectedTokens = List.of(
            Map.entry(FaunaTokenType.DOUBLE, "abc")
        );

        assertReader(invalidReader, expectedTokens, true);
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

        String invalidJson = "{\"@long\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        assertThrows(SerializationException.class, invalidReader::getValueAsLong);
    }

    private static void assertReader(FaunaParser reader,
        List<Map.Entry<FaunaTokenType, Object>> tokens,
        boolean assertExceptions) throws IOException {
        for (Map.Entry<FaunaTokenType, Object> entry : tokens) {
            reader.read();
            assertNotNull(entry.getKey());
            assertNotNull(reader.getCurrentTokenType());
            assertEquals(entry.getKey(), reader.getCurrentTokenType());

            switch (entry.getKey()) {
                case FIELD_NAME:
                case STRING:
                    if (assertExceptions) {
                        assertThrows(SerializationException.class, reader::getValueAsString);
                    } else {
                        assertEquals(entry.getValue(), reader.getValueAsString());
                    }
                    break;
                case INT:
                    if (assertExceptions) {
                        assertThrows(SerializationException.class, reader::getValueAsInt);
                    } else {
                        assertEquals(entry.getValue(), reader.getValueAsInt());
                    }
                    break;
                case TRUE:
                case FALSE:
                    if (assertExceptions) {
                        assertThrows(SerializationException.class, reader::getValueAsBoolean);
                    } else {
                        assertEquals(entry.getValue(), reader.getValueAsBoolean());
                    }
                    break;
                case DATE:
                    if (assertExceptions) {
                        assertThrows(SerializationException.class, reader::getValueAsLocalDate);
                    } else {
                        assertEquals(entry.getValue(), reader.getValueAsLocalDate());
                    }
                    break;
                case TIME:
                    if (assertExceptions) {
                        assertThrows(SerializationException.class, reader::getValueAsTime);
                    } else {
                        assertEquals(entry.getValue(), reader.getValueAsTime());
                    }
                    break;
                case DOUBLE:
                    if (assertExceptions) {
                        assertThrows(SerializationException.class, reader::getValueAsDouble);
                    } else {
                        assertEquals(entry.getValue(), reader.getValueAsDouble());
                    }
                    break;
                case LONG:
                    assertEquals(entry.getValue(), reader.getValueAsLong());
                    break;
                default:
                    assertNull(entry.getValue() == null);
                    break;
            }
        }

        assertFalse(reader.read());
    }

}