package com.fauna.codec.codecs;

import com.fauna.codec.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ListCodecTest {

    CodecRegistry cr = new DefaultCodecRegistry();
    CodecProvider cp = new DefaultCodecProvider(cr);

    @Test
    @SuppressWarnings("unchecked")
    public void roundtrip_listOfIntegers() throws IOException {
        var wire = "[{\"@int\":\"42\"}]";
        var codec = cp.get(List.class, Integer.class);
        List<Integer> decoded = Helpers.decode(codec, wire);
        var encoded = Helpers.encode(codec, decoded);
        assertEquals(wire, encoded);
    }
}
