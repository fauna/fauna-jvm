package com.fauna.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PageTest extends TypeTestBase {

    @Test
    public void page_doesNotPlayNiceWithJackson()
            throws JsonProcessingException {
        // Page no longer plays nice with Jackson, but we use our Codec/parser instead.
        var page = new Page<>(List.of(1), "next");
        var result = mapper.writeValueAsString(page);
        assertEquals(
                "{\"data\":[1],\"after\":{\"empty\":false,\"present\":true}}",
                result);
    }

    @Test
    public void page_equals_NotAPage() {
        Page page = new Page<>(List.of(1), "next");
        assertNotEquals("notapage", page);
    }
}
