package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.codec.Helpers;
import com.fauna.exception.CodecException;
import com.fauna.types.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PageCodecTest extends TestBase {
    public static final String PAGE_WIRE =
            "{\"@set\":{\"data\":[{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}],\"after\": null}}";
    public static final String DOCUMENT_WIRE =
            "{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}";
    public static final String ARRAY_WIRE =
            "[{\"@int\":\"1\"},{\"@int\":\"2\"}]";

    public static final ClassWithAttributes PERSON_WITH_ATTRIBUTES =
            new ClassWithAttributes("foo", "bar", 42);
    public static final Page<ClassWithAttributes> PAGE =
            new Page<>(List.of(PERSON_WITH_ATTRIBUTES), null);

    public static <T> Codec<Page<T>> pageCodecOf(Class<T> clazz) {
        //noinspection unchecked,rawtypes
        return (Codec<Page<T>>) (Codec) DefaultCodecProvider.SINGLETON.get(
                Page.class, new Type[] {clazz});
    }

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, ClassWithAttributes.class,
                        "null", null, null),
                Arguments.of(TestType.Decode, ClassWithAttributes.class,
                        PAGE_WIRE, PAGE, null),
                Arguments.of(TestType.Decode, ClassWithAttributes.class,
                        DOCUMENT_WIRE, PAGE, null),
                Arguments.of(TestType.Decode, Integer.class, ARRAY_WIRE,
                        new Page<>(List.of(1, 2), null), null),
                Arguments.of(TestType.Decode, Integer.class, "{\"@int\":\"1\"}",
                        new Page<>(List.of(1), null), null)
        );
    }

    @ParameterizedTest(name = "PageCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T, E extends Exception> void page_runTestCases(TestType testType,
                                                           Class<T> pageElemClass,
                                                           String wire,
                                                           Object obj,
                                                           E exception)
            throws IOException {
        var codec = pageCodecOf(pageElemClass);
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(pageCodecOf(Object.class));
    }

    @ParameterizedTest(name = "PageCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void page_runUnsupportedTypeTestCases(String wire, FaunaType type)
            throws IOException {
        var exMsg = MessageFormat.format(
                "Unable to decode `{0}` with `PageCodec<Object>`. Supported types for codec are [Array, Boolean, Bytes, Date, Double, Document, Int, Long, Module, Null, Object, Ref, Set, String, Time].",
                type);
        runCase(TestType.Decode, pageCodecOf(Object.class), wire, null,
                new CodecException(exMsg));
    }

    @Test
    public void page_decodeUnmaterializedSet()
    {
        var token = "aftertoken";
        var wire = "{\"@set\":\"" + token + "\"}";
        var codec = pageCodecOf(Object.class);
        var decoded = Helpers.decode(codec, wire);
        assertEquals(token, decoded.getAfter().get().getToken());
        assertEquals(0, decoded.getData().size());
    }
}
