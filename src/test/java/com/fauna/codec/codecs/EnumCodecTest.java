package com.fauna.codec.codecs;

import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class EnumCodecTest extends TestBase {

    enum TestEnum {
        Foo
    }

    @Test
    public void class_encodeEnum() throws IOException {
        var codec = DefaultCodecProvider.SINGLETON.get(TestEnum.class);
        var wire = "\"Foo\"";
        var obj = TestEnum.Foo;
        runCase(TestType.RoundTrip, codec, wire, obj, null);
    }
}
