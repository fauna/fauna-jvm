package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.query.builder.QueryArr;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fauna.query.builder.Query.fql;

public class QueryArrCodecTest extends TestBase {
    public static final Codec<QueryArr> QUERY_ARR_CODEC = DefaultCodecProvider.SINGLETON.get(QueryArr.class);

    public static final String QUERY_ARR_BASIC_WIRE = "{\"array\":[{\"fql\":[{\"value\":{\"@int\":\"42\"}}]}]}";
    public static final QueryArr QUERY_ARR_BASIC = QueryArr.of(List.of(fql("${n}", Map.of("n", 42))));

    public static final String QUERY_ARR_NESTED_WIRE = "{\"array\":[{\"array\":[{\"fql\":[{\"value\":{\"@int\":\"42\"}}]}]}]}";
    public static final QueryArr QUERY_ARR_NESTED = QueryArr.of(List.of(QueryArr.of(List.of(fql("${n}", Map.of("n", 42))))));

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.Encode, QUERY_ARR_CODEC, QUERY_ARR_BASIC_WIRE, QUERY_ARR_BASIC, null),
                Arguments.of(TestType.Encode, QUERY_ARR_CODEC, QUERY_ARR_NESTED_WIRE, QUERY_ARR_NESTED, null)
        );
    }

    @ParameterizedTest(name = "QueryArrCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void queryArr_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
