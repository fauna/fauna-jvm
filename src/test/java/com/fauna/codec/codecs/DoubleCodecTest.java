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

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(DOUBLE_CODEC);
    }

    @ParameterizedTest(name = "DoubleCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void double_runUnsupportedTypeTestCases(String wire, FaunaType type) throws IOException {
        var exMsg = MessageFormat.format("Unable to decode `{0}` with `DoubleCodec<Double>`. Supported types for codec are [Double, Int, Long, Null].", type);
        runCase(TestType.Decode, DOUBLE_CODEC, wire, null, new ClientException(exMsg));
    }
}
