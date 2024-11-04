package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.LONG_WIRE;


public class LongCodecTest extends TestBase {
    public static final Codec<Long> LONG_CODEC =
            DefaultCodecProvider.SINGLETON.get(Long.class);

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, LONG_CODEC,
                        LONG_WIRE(Long.MAX_VALUE), Long.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, LONG_CODEC,
                        LONG_WIRE(Long.MIN_VALUE), Long.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, LONG_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "LongCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T, E extends Exception> void long_runTestCases(TestType testType,
                                                           Codec<T> codec,
                                                           String wire,
                                                           Object obj,
                                                           E exception)
            throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(LONG_CODEC);
    }

    @ParameterizedTest(name = "LongCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void long_runUnsupportedTypeTestCases(String wire, FaunaType type)
            throws IOException {
        var exMsg = MessageFormat.format(
                "Unable to decode `{0}` with `LongCodec<Long>`. Supported types for codec are [Int, Long, Null].",
                type);
        runCase(TestType.Decode, LONG_CODEC, wire, null,
                new CodecException(exMsg));
    }
}
