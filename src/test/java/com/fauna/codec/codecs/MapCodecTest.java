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
import java.util.Map;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.ESCAPED_OBJECT_WIRE_WITH;


public class MapCodecTest extends TestBase {

    public static final Codec<Map<String, Integer>> MAP_INT_CODEC =
            (Codec<Map<String, Integer>>) (Codec) DefaultCodecProvider.SINGLETON.get(
                    Map.class, new Type[] {String.class, Integer.class});
    public static final Codec<Map<String, String>> MAP_STRING_CODEC =
            (Codec<Map<String, String>>) (Codec) DefaultCodecProvider.SINGLETON.get(
                    Map.class, new Type[] {String.class, String.class});


    public static Stream<Arguments> testCases() {
        return Stream.of(Arguments.of(TestType.RoundTrip, MAP_INT_CODEC,
                "{\"key1\":{\"@int\":\"42\"}}", Map.of("key1", 42), null));
    }

    @ParameterizedTest(name = "MapCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T, E extends Exception> void map_runTestCases(TestType testType,
                                                          Codec<T> codec,
                                                          String wire,
                                                          Object obj,
                                                          E exception)
            throws IOException {
        runCase(testType, codec, wire, obj, exception);

    }

    @ParameterizedTest
    @MethodSource("tags")
    public void map_escapeOnReservedKey(String tag) throws IOException {
        runCase(TestType.RoundTrip, MAP_STRING_CODEC,
                ESCAPED_OBJECT_WIRE_WITH(tag), Map.of(tag, "not"), null);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(MAP_STRING_CODEC);
    }

    @ParameterizedTest(name = "MapCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void map_runUnsupportedTypeTestCases(String wire, FaunaType type)
            throws IOException {
        var exMsg = MessageFormat.format(
                "Unable to decode `{0}` with `MapCodec<String>`. Supported types for codec are [Null, Object].",
                type);
        runCase(TestType.Decode, MAP_STRING_CODEC, wire, null,
                new CodecException(exMsg));
    }
}
