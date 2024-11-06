package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.query.builder.QueryObj;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static com.fauna.query.builder.Query.fql;

public class QueryObjCodecTest extends TestBase {
    public static final Codec<QueryObj> QUERY_OBJ_CODEC =
            DefaultCodecProvider.SINGLETON.get(QueryObj.class);

    public static final String QUERY_OBJ_BASIC_WIRE =
            "{\"object\":{\"calc\":{\"fql\":[{\"value\":{\"@int\":\"42\"}}]}}}";
    public static final QueryObj QUERY_OBJ_BASIC =
            QueryObj.of(Map.of("calc", fql("${n}", Map.of("n", 42))));

    public static final String QUERY_OBJ_NESTED_WIRE =
            "{\"object\":{\"outer\":{\"object\":{\"inner\":{\"fql\":[{\"value\":{\"@int\":\"42\"}}]}}}}}";
    public static final QueryObj QUERY_OBJ_NESTED = QueryObj.of(Map.of("outer",
            QueryObj.of(Map.of("inner", fql("${n}", Map.of("n", 42))))));

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.Encode, QUERY_OBJ_CODEC,
                        QUERY_OBJ_BASIC_WIRE, QUERY_OBJ_BASIC, null),
                Arguments.of(TestType.Encode, QUERY_OBJ_CODEC,
                        QUERY_OBJ_NESTED_WIRE, QUERY_OBJ_NESTED, null)
        );
    }

    @ParameterizedTest(name = "QueryObjCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T, E extends Exception> void query_runTestCases(TestType testType,
                                                            Codec<T> codec,
                                                            String wire,
                                                            Object obj,
                                                            E exception)
            throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
