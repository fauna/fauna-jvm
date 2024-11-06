package com.fauna.codec.codecs;

import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.stream.Stream;


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


    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(
                DefaultCodecProvider.SINGLETON.get(TestEnum.class));
    }

    @ParameterizedTest(name = "EnumCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void enum_runUnsupportedTypeTestCases(String wire, FaunaType type)
            throws IOException {
        var exMsg = MessageFormat.format(
                "Unable to decode `{0}` with `EnumCodec<TestEnum>`. Supported types for codec are [Null, String].",
                type);
        runCase(TestType.Decode,
                DefaultCodecProvider.SINGLETON.get(TestEnum.class), wire, null,
                new CodecException(exMsg));
    }
}
