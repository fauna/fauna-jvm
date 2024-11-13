package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.event.EventSource;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.Document;
import com.fauna.types.DocumentRef;
import com.fauna.types.Module;
import com.fauna.types.NamedDocument;
import com.fauna.types.NamedDocumentRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DynamicCodecTest extends TestBase {
    public static final Codec<Object> DYNAMIC_CODEC =
            DefaultCodecProvider.SINGLETON.get(Object.class);

    // Class with attributes
    public static final String CLASS_WITH_ATTRIBUTES_WIRE =
            "{\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}";
    public static final ClassWithAttributes CLASS_WITH_ATTRIBUTES =
            new ClassWithAttributes("foo", "bar", 42);

    // Doc
    public static final String DOCUMENT_WIRE =
            "{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}";
    public static final Document DOCUMENT = new Document(
            "123",
            new Module("Foo"),
            Instant.parse("2023-12-15T01:01:01.0010010Z"),
            Map.of("first_name", "foo", "last_name", "bar", "age", 42)
    );
    public static final DocumentRef DOCUMENT_REF =
            new DocumentRef("123", new Module("Foo"));
    public static final String DOCUMENT_REF_WIRE =
            "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"}}}";

    // Named doc
    public static final String NAMED_DOCUMENT_WIRE =
            "{\"@doc\":{\"name\":\"Boogles\",\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"coll\":{\"@mod\":\"Foo\"},\"first_name\":\"foo\",\"last_name\":\"bar\"}}";
    public static final NamedDocument NAMED_DOCUMENT = new NamedDocument(
            "Boogles",
            new Module("Foo"),
            Instant.parse("2023-12-15T01:01:01.0010010Z"),
            Map.of("first_name", "foo", "last_name", "bar")
    );
    public static final String NAMED_DOCUMENT_REF_WIRE =
            "{\"@ref\":{\"name\":\"Boogles\",\"coll\":{\"@mod\":\"Foo\"}}}";
    public static final NamedDocumentRef NAMED_DOCUMENT_REF =
            new NamedDocumentRef("Boogles", new Module("Foo"));


    // Null doc
    public static final String NULL_DOC_WIRE =
            "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"exists\":false,\"cause\":\"not found\"}}";
    public static final NullDocumentException NULL_DOC_EXCEPTION =
            new NullDocumentException("123", new Module("Foo"), "not found");

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.Decode, DYNAMIC_CODEC, DOCUMENT_WIRE,
                        DOCUMENT, null),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC, DOCUMENT_REF_WIRE,
                        DOCUMENT_REF, null),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC,
                        NAMED_DOCUMENT_WIRE, NAMED_DOCUMENT, null),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC,
                        NAMED_DOCUMENT_REF_WIRE, NAMED_DOCUMENT_REF, null),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC, NULL_DOC_WIRE,
                        null, NULL_DOC_EXCEPTION),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC,
                        "{\"@stream\":\"token\"}",
                        new EventSource("token"), null),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC,
                        "{\"@bytes\": \"RmF1bmE=\"}",
                        new byte[] {70, 97, 117, 110, 97}, null)
        );
    }

    @ParameterizedTest(name = "DynamicCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T, E extends Exception> void dynamic_runTestCases(TestType testType,
                                                              Codec<T> codec,
                                                              String wire,
                                                              Object obj,
                                                              E exception)
            throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    @Test
    public void dynamic_shouldSupportAllTypes() {
        var arr = Arrays.stream(FaunaType.values())
                .filter(f -> Arrays.stream(DYNAMIC_CODEC.getSupportedTypes())
                        .noneMatch(f::equals)).toArray();
        assertEquals("[]", Arrays.toString(arr));
    }
}
