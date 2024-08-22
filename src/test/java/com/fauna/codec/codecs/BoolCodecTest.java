package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;


public class BoolCodecTest extends TestBase {
    public static final Codec<Boolean>  BOOL_CODEC = DefaultCodecProvider.SINGLETON.get(Boolean.class);
    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, BOOL_CODEC, "true", true, null),
                Arguments.of(TestType.RoundTrip, BOOL_CODEC, "false", false, null),
                Arguments.of(TestType.RoundTrip, BOOL_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "BoolCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void bool_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
