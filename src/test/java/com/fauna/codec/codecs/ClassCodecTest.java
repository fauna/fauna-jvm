package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithParameterizedFields;
import com.fauna.codec.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassCodecTest {

    CodecRegistry cr = new DefaultCodecRegistry();
    CodecProvider cp = new DefaultCodecProvider(cr);

    @Test
    public void roundtrip_classWithParameterizedFields() throws IOException {
        var wire = "{\"first_name\":\"foo\",\"a_list\":[\"item1\"],\"a_map\":{\"key1\":{\"@int\":\"42\"}}}";
        var codec = cp.get(ClassWithParameterizedFields.class);
        ClassWithParameterizedFields decoded = Helpers.decode(codec, wire);
        var encoded = Helpers.encode(codec, decoded);
        assertEquals(wire, encoded);
    }
}
