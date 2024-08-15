package com.fauna.codec.codecs;

import com.fauna.codec.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MapCodecTest {

    CodecRegistry cr = new DefaultCodecRegistry();
    CodecProvider cp = new DefaultCodecProvider(cr);

    @Test
    @SuppressWarnings("unchecked")
    public void roundtrip_mapOfIntegers() throws IOException {
        var wire = "{\"key1\":{\"@int\":\"42\"}}";
        var codec = cp.get(Map.class, Integer.class);
        Map<String,Integer> decoded = Helpers.decode(codec, wire);
        var encoded = Helpers.encode(codec, decoded);
        assertEquals(wire, encoded);
    }
}
