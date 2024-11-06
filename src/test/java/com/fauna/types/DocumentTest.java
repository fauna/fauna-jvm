package com.fauna.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentTest extends TypeTestBase {

    @Test
    public void document_playsNiceWithJackson() throws JsonProcessingException {
        var doc = new Document(
                "123",
                new Module("MyColl"),
                Instant.parse("2024-01-23T13:33:10.300Z"),
                Map.of("some_key", "some_val")
        );

        var result = mapper.writeValueAsString(doc);
        assertEquals(
                "{\"data\":{\"some_key\":\"some_val\"},\"ts\":1706016790.300000000,\"collection\":{\"name\":\"MyColl\"},\"id\":\"123\"}",
                result);
    }
}
