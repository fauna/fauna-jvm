package com.fauna.codec.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.response.QueryResponseInternal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PassThroughDeserializerTest {

    @Test
    public void smoke() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var json = "{\"error\":{\"code\":\"err\",\"abort\":{\"@int\":42}}}";
        var res = mapper.readValue(json, QueryResponseInternal.class);
        Assertions.assertEquals("{\"@int\":42}",res.error.getAbortRaw());
    }
}
