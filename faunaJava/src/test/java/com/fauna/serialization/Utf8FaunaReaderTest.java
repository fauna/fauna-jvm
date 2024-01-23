package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;


class Utf8FaunaReaderTest {

    @Test
    public void testGetValueAsString() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("\"hello\"".getBytes());
        Utf8FaunaReader reader = new Utf8FaunaReader(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
                Map.entry(FaunaTokenType.STRING, "hello")
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testReadInt() throws IOException {
        String s = "{\"@int\": \"123\"}";
        InputStream inputStream = new ByteArrayInputStream(s.getBytes());
        Utf8FaunaReader reader = new Utf8FaunaReader(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
                Map.entry(FaunaTokenType.INT, 123)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsBooleanTrue() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("true".getBytes());
        Utf8FaunaReader reader = new Utf8FaunaReader(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
                Map.entry(FaunaTokenType.TRUE, true)
        );

        assertReader(reader, expectedTokens);
    }

    @Test
    public void testGetValueAsBooleanFalse() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("false".getBytes());
        Utf8FaunaReader reader = new Utf8FaunaReader(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
                Map.entry(FaunaTokenType.FALSE, false)
        );

        assertReader(reader, expectedTokens);
    }

    private static void assertReader(Utf8FaunaReader reader, List<Map.Entry<FaunaTokenType, Object>> tokens) throws IOException {
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
                default:
                    assertNull(entry.getValue() == null);
                    break;
            }
        }

        assertFalse(reader.read());
    }

}