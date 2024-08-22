package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.DOUBLE_WIRE;


public class FloatCodecTest extends TestBase {
    public static final Codec<Float> FLOAT_CODEC = DefaultCodecProvider.SINGLETON.get(Float.class);
    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, FLOAT_CODEC, DOUBLE_WIRE(Float.MAX_VALUE), Float.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, FLOAT_CODEC, DOUBLE_WIRE(Float.MIN_VALUE), Float.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, FLOAT_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "FloatCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void float_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
