package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Instant;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.TIME_WIRE;


public class InstantCodecTest extends TestBase {
    public static final Codec<Instant> INSTANT_CODEC = DefaultCodecProvider.SINGLETON.get(Instant.class);
    public static String TIME_STRING_PACIFIC = "2023-12-03T05:52:10.000001-09:00";
    public static String TIME_STRING_UTC = "2023-12-03T14:52:10.000001Z";
    public static Instant INSTANT = Instant.parse(TIME_STRING_PACIFIC);


    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, INSTANT_CODEC, TIME_WIRE(TIME_STRING_UTC), INSTANT, null),
                Arguments.of(TestType.RoundTrip, INSTANT_CODEC, "null", null, null),
                Arguments.of(TestType.Decode, INSTANT_CODEC, TIME_WIRE(TIME_STRING_PACIFIC), INSTANT, null)
        );
    }

    @ParameterizedTest(name = "InstantCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void instant_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
