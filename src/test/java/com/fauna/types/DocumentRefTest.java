package com.fauna.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentRefTest extends TypeTestBase {

    @Test
    public void docRef_playsNiceWithJackson() throws JsonProcessingException {
        var doc = new DocumentRef(
                "123",
                new Module("MyColl")
        );

        var result = mapper.writeValueAsString(doc);
        assertEquals("{\"collection\":{\"name\":\"MyColl\"},\"id\":\"123\"}",
                result);
    }
}
