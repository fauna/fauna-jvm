package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
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
}
