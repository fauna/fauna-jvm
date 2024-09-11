package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.Stream;


public class OptionalCodecTest extends TestBase {
    public static final Codec<Optional<String>> OPTIONAL_STRING_CODEC = (Codec<Optional<String>>) (Codec) DefaultCodecProvider.SINGLETON.get(Optional.class, new Type[]{String.class});

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

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(OPTIONAL_STRING_CODEC);
    }

    @ParameterizedTest(name = "OptionalCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void optional_runUnsupportedTypeTestCases(String wire, FaunaType type) throws IOException {
        // This codec will pass through the supported types of the underlying codec
        var exMsg = MessageFormat.format("Unable to decode `{0}` with `StringCodec<String>`. Supported types for codec are [Bytes, Null, String].", type);
        runCase(TestType.Decode, OPTIONAL_STRING_CODEC, wire, null, new CodecException(exMsg));
    }
}
