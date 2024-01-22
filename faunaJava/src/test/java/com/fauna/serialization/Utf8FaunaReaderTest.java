package com.fauna.serialization;

import com.fauna.common.enums.FaunaTokenType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;


class Utf8FaunaReaderTest {

    public static void main(String[] args) throws IOException {
        InputStream inputStream = new ByteArrayInputStream("\"hello\"".getBytes());
        Utf8FaunaReader reader = new Utf8FaunaReader(inputStream);

        List<Map.Entry<FaunaTokenType, Object>> expectedTokens = List.of(
                Map.entry(FaunaTokenType.STRING, "hello")
        );

        assertReader(reader, expectedTokens);
    }

    private static void assertReader(Utf8FaunaReader reader, List<Map.Entry<FaunaTokenType, Object>> tokens) throws IOException {
        for (Map.Entry<FaunaTokenType, Object> entry : tokens) {
            reader.read();
            Objects.requireNonNull(entry.getKey());
            Objects.requireNonNull(reader.getCurrentTokenType());
            assert entry.getKey().equals(reader.getCurrentTokenType());

            switch (entry.getKey()) {
                case FIELD_NAME:
                case STRING:
                    assert entry.getValue().equals(reader.getValueAsString());
                    break;
                default:
                    assert entry.getValue() == null;
                    break;
            }
        }

        assert !reader.read();
    }

}