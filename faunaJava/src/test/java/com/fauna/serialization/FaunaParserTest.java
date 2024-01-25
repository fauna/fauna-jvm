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

        String invalidJson = "{\"@int\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        assertThrows(RuntimeException.class, invalidReader::getValueAsInt);
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
    public void testeGetValueAsLocalDate() throws IOException {
        String s = "{\"@date\":\"2024-01-23\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.DATE, LocalDate.of(2024, 01, 23))
        );

        assertReader(reader, expectedTokens);

        String invalidJson = "{\"@date\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        assertThrows(RuntimeException.class, invalidReader::getValueAsLocalDate);
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

        assertReader(reader, expectedTokens);

        String invalidJson = "{\"@time\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        assertThrows(RuntimeException.class, invalidReader::getValueAsLocalDate);
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

        String invalidJson = "{\"@double\": \"abc\"}";
        InputStream invalidInputStream = new ByteArrayInputStream(invalidJson.getBytes());
        FaunaParser invalidReader = new FaunaParser(invalidInputStream);

        assertThrows(RuntimeException.class, invalidReader::getValueAsDouble);
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

        assertThrows(RuntimeException.class, invalidReader::getValueAsLong);
    }

    @Test
    public void testGetValueAsModule() throws IOException {
        String s = "{\"@mod\": \"MyModule\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        FaunaParser reader = new FaunaParser(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
            Map.entry(FaunaTokenType.MODULE, new Module("MyModule"))
        );

        assertReader(reader, expectedTokens);
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