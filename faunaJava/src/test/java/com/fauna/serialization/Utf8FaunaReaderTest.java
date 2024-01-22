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

    private static void assertReader(Utf8FaunaReader reader, List<Map.Entry<FaunaTokenType, Object>> tokens) {
        for (Map.Entry<FaunaTokenType, Object> entry : tokens) {
            reader.read();
            assertNotNull(entry.getKey());
            assertNotNull(reader.getCurrentTokenType());
            assertEquals(entry.getKey(), FaunaTokenType.STRING);

            switch (entry.getKey()) {
                case FIELD_NAME:
                case STRING:
                    assertEquals(entry.getValue(), reader.getValueAsString());
                    break;
                default:
                    assertNull(entry.getValue() == null);
                    break;
            }
        }

        assertFalse(reader.read());
    }

}