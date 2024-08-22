package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.types.Page;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;


public class PageCodecTest extends TestBase {
    public static final Codec<Page<ClassWithAttributes>> PAGE_CODEC = (Codec<Page<ClassWithAttributes>>) (Codec) DefaultCodecProvider.SINGLETON.get(Page.class, ClassWithAttributes.class);
    public static final String PAGE_WIRE = "{\"@set\":{\"data\":[{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}],\"after\": null}}";
    public static final String DOCUMENT_WIRE = "{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}";
    public static final ClassWithAttributes PERSON_WITH_ATTRIBUTES = new ClassWithAttributes("foo","bar",42);
    public static final Page<ClassWithAttributes> PAGE = new Page<>(List.of(PERSON_WITH_ATTRIBUTES),null);

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, PAGE_CODEC, "null", null, null),
                Arguments.of(TestType.Decode, PAGE_CODEC, PAGE_WIRE, PAGE, null),
                Arguments.of(TestType.Decode, PAGE_CODEC, DOCUMENT_WIRE, PAGE, null)
        );
    }

    @ParameterizedTest(name = "PageCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void page_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
