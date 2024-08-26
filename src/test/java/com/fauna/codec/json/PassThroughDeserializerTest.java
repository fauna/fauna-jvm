package com.fauna.codec.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.response.wire.QueryResponseWire;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PassThroughDeserializerTest {

    @Test
    public void deserializeObjectAsRawString() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var json = "{\"error\":{\"code\":\"err\",\"abort\":{\"@int\":42}}}";
        var res = mapper.readValue(json, QueryResponseWire.class);
        Assertions.assertEquals("{\"@int\":42}", res.getError().getAbort().get());
    }

    @Test
    public void deserializeStringAsRawString() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var json = "{\"error\":{\"code\":\"err\",\"abort\":\"stringy\"}}";
        var res = mapper.readValue(json, QueryResponseWire.class);
        Assertions.assertEquals("\"stringy\"", res.getError().getAbort().get());
    }

    @Test
    public void deserializeNull() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var json = "{\"error\":{\"code\":\"err\",\"abort\":null}}";
        var res = mapper.readValue(json, QueryResponseWire.class);
        Assertions.assertTrue(res.getError().getAbort().isEmpty());
    }
}
