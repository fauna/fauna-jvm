package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.INT_WIRE;


public class CharCodecTest extends TestBase {
    public static final Codec<Character> CHAR_CODEC = DefaultCodecProvider.SINGLETON.get(Character.class);
    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, CHAR_CODEC, INT_WIRE(84), 'T', null),
                Arguments.of(TestType.RoundTrip, CHAR_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "CharCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void char_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
