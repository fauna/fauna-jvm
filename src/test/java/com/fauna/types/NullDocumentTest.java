package com.fauna.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NullDocumentTest extends TypeTestBase {

    @Test
    public void nullDoc_playsNiceWithJackson() throws JsonProcessingException {
        var nonNull = new NullDocument<>("123", new Module("MyColl"), "not found");

        var result = mapper.writeValueAsString(nonNull);
        assertEquals("{\"id\":\"123\",\"cause\":\"not found\",\"collection\":{\"name\":\"MyColl\"}}", result);
    }
}
