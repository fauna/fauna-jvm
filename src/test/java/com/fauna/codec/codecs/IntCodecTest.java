package com.fauna.codec.codecs;

import com.fauna.codec.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntCodecTest {

    CodecRegistry cr = new DefaultCodecRegistry();
    CodecProvider cp = new DefaultCodecProvider(cr);

    @Test
    public void roundtrip_int() throws IOException {
        var wire = "{\"@int\":\"42\"}";
        var codec = cp.get(int.class);
        int decoded = Helpers.decode(codec, wire);
        var encoded = Helpers.encode(codec, decoded);
        assertEquals(wire, encoded);
    }
}
