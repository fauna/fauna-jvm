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

public class ByteArrayCodecTest extends TestBase {
    public static final Codec<byte[]> BYTE_ARRAY_CODEC =
            DefaultCodecProvider.SINGLETON.get(byte[].class);

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, BYTE_ARRAY_CODEC,
                        "{\"@bytes\":\"RmF1bmE=\"}",
                        new byte[] {70, 97, 117, 110, 97}, null),
                Arguments.of(TestType.RoundTrip, BYTE_ARRAY_CODEC, "null", null,
                        null)
        );
    }

    @ParameterizedTest(name = "ByteArrayCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T, E extends Exception> void byteArray_runTestCases(
            TestType testType, Codec<T> codec, String wire, Object obj,
            E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(BYTE_ARRAY_CODEC);
    }

    @ParameterizedTest(name = "ByteArrayCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void byteArray_runUnsupportedTypeTestCases(String wire,
                                                      FaunaType type)
            throws IOException {
        var exMsg = MessageFormat.format(
                "Unable to decode `{0}` with `ByteArrayCodec<byte[]>`. Supported types for codec are [Bytes, Null].",
                type);
        runCase(TestType.Decode, BYTE_ARRAY_CODEC, wire, null,
                new CodecException(exMsg));
    }
}
