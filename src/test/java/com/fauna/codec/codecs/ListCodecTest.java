package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;


public class ListCodecTest extends TestBase {

    public static final Codec<List<Integer>> LIST_INT_CODEC = (Codec<List<Integer>>) (Codec) DefaultCodecProvider.SINGLETON.get(List.class, int.class);


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
}
