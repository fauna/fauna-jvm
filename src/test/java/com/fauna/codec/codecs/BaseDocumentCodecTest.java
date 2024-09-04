package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.ClientException;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.*;
import com.fauna.types.Module;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;


public class BaseDocumentCodecTest extends TestBase {
    public static final Codec<BaseDocument> BASE_DOCUMENT_CODEC = DefaultCodecProvider.SINGLETON.get(BaseDocument.class);
    
    // Docs
    public static final String DOCUMENT_WIRE = "{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}";
    public static final Document DOCUMENT = new Document(
            "123",
            new Module("Foo"),
            Instant.parse("2023-12-15T01:01:01.0010010Z"),
            Map.of("first_name","foo", "last_name", "bar","age",42)
    );
    public static final String NULL_DOC_WIRE = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"exists\":false,\"cause\":\"not found\"}}";
    public static final NullDocumentException NULL_DOC_EXCEPTION = new NullDocumentException("123", new Module("Foo"), "not found");
    
    
    // Named docs
    public static final String NAMED_DOCUMENT_WIRE = "{\"@doc\":{\"name\":\"Boogles\",\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"coll\":{\"@mod\":\"Foo\"},\"first_name\":\"foo\",\"last_name\":\"bar\"}}";
    public static final NamedDocument NAMED_DOCUMENT = new NamedDocument(
            "Boogles",
            new Module("Foo"),
            Instant.parse("2023-12-15T01:01:01.0010010Z"),
            Map.of("first_name","foo", "last_name", "bar")
    );
    
    // Refs
    public static final String DOCUMENT_REF_WIRE  = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"}}}";

    public static final String NAMED_DOCUMENT_REF_WIRE = "{\"@ref\":{\"name\":\"Boogles\",\"coll\":{\"@mod\":\"Foo\"}}}";
    public static final ClassWithAttributes PERSON_WITH_ATTRIBUTES = new ClassWithAttributes("foo","bar",42);
    

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.Decode, BASE_DOCUMENT_CODEC, DOCUMENT_WIRE, DOCUMENT, null),
                Arguments.of(TestType.Decode, BASE_DOCUMENT_CODEC, NAMED_DOCUMENT_WIRE, NAMED_DOCUMENT, null),
                Arguments.of(TestType.Decode, BASE_DOCUMENT_CODEC, NULL_DOC_WIRE, null, NULL_DOC_EXCEPTION),
                Arguments.of(TestType.Decode, BASE_DOCUMENT_CODEC, DOCUMENT_REF_WIRE, null, new ClientException("Unexpected type `class com.fauna.types.DocumentRef` decoding with `BaseDocumentCodec<BaseDocument>`")),
                Arguments.of(TestType.Encode, BASE_DOCUMENT_CODEC, DOCUMENT_REF_WIRE, DOCUMENT, null),
                Arguments.of(TestType.Encode, BASE_DOCUMENT_CODEC, NAMED_DOCUMENT_REF_WIRE, NAMED_DOCUMENT, null)
        );
    }

    @ParameterizedTest(name = "BaseDocumentCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void baseDoc_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
}
