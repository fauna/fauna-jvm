package com.fauna.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PageTest extends TypeTestBase {

    @Test
    public void page_playsNiceWithJackson() throws JsonProcessingException {
        var page = new Page<>(List.of(1), "next");
        var result = mapper.writeValueAsString(page);
        assertEquals("{\"data\":[1],\"after\":\"next\"}", result);
    }

    @Test
    public void page_equals_NotAPage() {
        Page page = new Page<>(List.of(1), "next");
        assertFalse(page.equals("notapage"));
    }
}
