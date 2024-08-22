package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.stream.Stream;

public class LocalDateCodecTest extends TestBase {
    public static final Codec<LocalDate> LOCAL_DATE_CODEC = DefaultCodecProvider.SINGLETON.get(LocalDate.class);

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, LOCAL_DATE_CODEC, "{\"@date\":\"2023-12-03\"}", LocalDate.parse("2023-12-03"), null),
                Arguments.of(TestType.RoundTrip, LOCAL_DATE_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "LocalDateCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void localDate_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
