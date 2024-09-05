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

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(FLOAT_CODEC);
    }

    @ParameterizedTest(name = "FloatCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void float_runUnsupportedTypeTestCases(String wire, FaunaType type) throws IOException {
        var exMsg = MessageFormat.format("Unable to decode `{0}` with `FloatCodec<Float>`. Supported types for codec are [Double, Int, Long, Null].", type);
        runCase(TestType.Decode, FLOAT_CODEC, wire, null, new ClientException(exMsg));
    }
}
