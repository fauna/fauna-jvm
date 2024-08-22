package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;


public class OptionalCodecTest extends TestBase {
    public static final Codec<Optional<String>> OPTIONAL_STRING_CODEC = (Codec<Optional<String>>) (Codec) DefaultCodecProvider.SINGLETON.get(Optional.class, String.class);

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, OPTIONAL_STRING_CODEC, "null", Optional.empty(), null),
                Arguments.of(TestType.RoundTrip, OPTIONAL_STRING_CODEC, "\"Fauna\"", Optional.of("Fauna"), null)
        );
    }

    @ParameterizedTest(name = "OptionalCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void optional_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
