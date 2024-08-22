package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.INT_WIRE;


public class ShortCodecTest extends TestBase {
    public static final Codec<Short> SHORT_CODEC = DefaultCodecProvider.SINGLETON.get(Short.class);
    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, SHORT_CODEC, INT_WIRE((int) Short.MAX_VALUE), Short.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, SHORT_CODEC, INT_WIRE((int) Short.MIN_VALUE), Short.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, SHORT_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "ShortCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void short_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
