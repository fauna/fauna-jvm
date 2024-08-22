package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.INT_WIRE;

public class IntCodecTest extends TestBase {
    public static final Codec<Integer> INT_CODEC = DefaultCodecProvider.SINGLETON.get(Integer.class);
    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, INT_CODEC, INT_WIRE(Integer.MAX_VALUE), Integer.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, INT_CODEC, INT_WIRE(Integer.MIN_VALUE), Integer.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, INT_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "IntCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void int_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
