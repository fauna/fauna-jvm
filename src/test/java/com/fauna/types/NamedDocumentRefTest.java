package com.fauna.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamedDocumentRefTest extends TypeTestBase {

    @Test
    public void namedDocRef_playsNiceWithJackson() throws JsonProcessingException {
        var doc = new NamedDocumentRef(
                "AName",
                new Module("MyColl")
        );

        var result = mapper.writeValueAsString(doc);
        assertEquals("{\"collection\":{\"name\":\"MyColl\"},\"name\":\"AName\"}", result);
    }
}
