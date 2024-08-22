package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.ESCAPED_OBJECT_WIRE_WITH;


public class MapCodecTest extends TestBase {

    public static final Codec<Map<String,Integer>> MAP_INT_CODEC = (Codec<Map<String, Integer>>) (Codec) DefaultCodecProvider.SINGLETON.get(Map.class, Integer.class);
    public static final Codec<Map<String, String>> MAP_STRING_CODEC = (Codec<Map<String,String>>) (Codec) DefaultCodecProvider.SINGLETON.get(Map.class, String.class);


    public static Stream<Arguments> testCases() {
        return Stream.of(Arguments.of(TestType.RoundTrip, MAP_INT_CODEC, "{\"key1\":{\"@int\":\"42\"}}", Map.of("key1", 42), null));
    }

    @ParameterizedTest(name = "MapCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void map_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);

    }

    @ParameterizedTest
    @MethodSource("tags")
    public void map_escapeOnReservedKey(String tag) throws IOException {
        runCase(TestType.RoundTrip, MAP_STRING_CODEC, ESCAPED_OBJECT_WIRE_WITH(tag), Map.of(tag, "not"), null);
    }
}
