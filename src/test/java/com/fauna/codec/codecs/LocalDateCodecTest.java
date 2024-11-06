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
import java.time.LocalDate;
import java.util.stream.Stream;

public class LocalDateCodecTest extends TestBase {
    public static final Codec<LocalDate> LOCAL_DATE_CODEC =
            DefaultCodecProvider.SINGLETON.get(LocalDate.class);

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, LOCAL_DATE_CODEC,
                        "{\"@date\":\"2023-12-03\"}",
                        LocalDate.parse("2023-12-03"), null),
                Arguments.of(TestType.RoundTrip, LOCAL_DATE_CODEC, "null", null,
                        null)
        );
    }

    @ParameterizedTest(name = "LocalDateCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T, E extends Exception> void localDate_runTestCases(
            TestType testType, Codec<T> codec, String wire, Object obj,
            E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(LOCAL_DATE_CODEC);
    }

    @ParameterizedTest(name = "LocalDateCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void localDate_runUnsupportedTypeTestCases(String wire,
                                                      FaunaType type)
            throws IOException {
        var exMsg = MessageFormat.format(
                "Unable to decode `{0}` with `LocalDateCodec<LocalDate>`. Supported types for codec are [Date, Null].",
                type);
        runCase(TestType.Decode, LOCAL_DATE_CODEC, wire, null,
                new CodecException(exMsg));
    }
}
