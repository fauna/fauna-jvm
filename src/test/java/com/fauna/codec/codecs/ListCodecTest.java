package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.types.Document;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Stream;


public class ListCodecTest extends TestBase {

    public static final Codec<List<Integer>> LIST_INT_CODEC = (Codec<List<Integer>>) (Codec) DefaultCodecProvider.SINGLETON.get(List.class, new Type[]{int.class});

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, LIST_INT_CODEC, "[{\"@int\":\"42\"}]", List.of(42), null),
                Arguments.of(TestType.RoundTrip, LIST_INT_CODEC, "null", null, null)
        );
    }

    @ParameterizedTest(name = "ListCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void list_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(LIST_INT_CODEC);
    }

    @ParameterizedTest(name = "ListCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void list_runUnsupportedTypeTestCases(String wire, FaunaType type) throws IOException {
        var exMsg = MessageFormat.format("Unable to decode `{0}` with `ListCodec<Integer>`. Supported types for codec are [Array, Null].", type);
        runCase(TestType.Decode, LIST_INT_CODEC, wire, null, new CodecException(exMsg));
    }
}
