package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.query.builder.Query;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static com.fauna.query.builder.Query.fql;

public class StreamRequestCodecTest extends TestBase {
    public static final Codec<Query> QUERY_CODEC = DefaultCodecProvider.SINGLETON.get(Query.class);
    public static final Query QUERY = fql("let age = ${n}", Map.of("n", 42));
    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.Encode, QUERY_CODEC, "{\"fql\":[\"let age = \",{\"value\":{\"@int\":\"42\"}}]}", QUERY, null)
        );
    }

    @ParameterizedTest(name = "QueryCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void query_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
