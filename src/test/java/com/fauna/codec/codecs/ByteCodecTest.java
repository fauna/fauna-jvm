package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.codec.Helpers;
import com.fauna.exception.ClientException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.INT_WIRE;


public class ByteCodecTest extends TestBase {
    public static final Codec<Byte> BYTE_CODEC = DefaultCodecProvider.SINGLETON.get(Byte.class);

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, BYTE_CODEC, INT_WIRE((int) Byte.MAX_VALUE), Byte.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, BYTE_CODEC, INT_WIRE((int) Byte.MIN_VALUE), Byte.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, BYTE_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "ByteCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void byte_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(BYTE_CODEC);
    }

    @ParameterizedTest(name = "ByteCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void byte_runUnsupportedTypeTestCases(String wire, FaunaType type) throws IOException {
        var exMsg = MessageFormat.format("Unable to decode `{0}` with `ByteCodec<Byte>`. Supported types for codec are [Int, Null].", type);
        runCase(TestType.Decode, BYTE_CODEC, wire, null, new ClientException(exMsg));
    }
}
