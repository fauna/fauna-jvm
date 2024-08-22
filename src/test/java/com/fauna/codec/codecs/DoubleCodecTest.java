package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.DOUBLE_WIRE;


public class DoubleCodecTest extends TestBase {
    public static final Codec<Double> DOUBLE_CODEC = DefaultCodecProvider.SINGLETON.get(Double.class);
    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, DOUBLE_CODEC, DOUBLE_WIRE(Double.MAX_VALUE), Double.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, DOUBLE_CODEC, DOUBLE_WIRE(Double.MIN_VALUE), Double.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, DOUBLE_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "DoubleCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void double_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
