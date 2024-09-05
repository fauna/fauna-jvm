package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.stream.Stream;


public class StringCodecTest extends TestBase {
    public static final Codec<String> STRING_CODEC = DefaultCodecProvider.SINGLETON.get(String.class);
    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, STRING_CODEC, "\"Fauna\"", "Fauna", null),
                Arguments.of(TestType.RoundTrip, STRING_CODEC, "null", null, null),
                Arguments.of(TestType.Decode, STRING_CODEC, "{\"@bytes\":\"RmF1bmE=\"}", "RmF1bmE=", null)
        );
    }

    @ParameterizedTest(name = "StringCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void string_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(STRING_CODEC);
    }

    @ParameterizedTest(name = "StringCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void string_runUnsupportedTypeTestCases(String wire, FaunaType type) throws IOException {
        var exMsg = MessageFormat.format("Unable to decode `{0}` with `StringCodec<String>`. Supported types for codec are [Bytes, Null, String].", type);
        runCase(TestType.Decode, STRING_CODEC, wire, null, new ClientException(exMsg));
    }
}
