package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.exception.ClientException;
import com.fauna.types.*;
import com.fauna.types.Module;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

public class NullableDocumentCodecTest extends TestBase {
    public static final Codec<NullableDocument<Document>> NULLABLE_DOC_CODEC = (Codec)DefaultCodecProvider.SINGLETON.get(NullableDocument.class, new Type[]{Document.class});
    public static final Codec<NullableDocument<ClassWithAttributes>> NULLABLE_ClASS_CODEC = (Codec)DefaultCodecProvider.SINGLETON.get(NullableDocument.class, new Type[]{ClassWithAttributes.class});
    
    // Class with attributes
    public static final String CLASS_WITH_ATTRIBUTES_WIRE = "{\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}";
    public static final ClassWithAttributes CLASS_WITH_ATTRIBUTES = new ClassWithAttributes("foo","bar",42);

    // Doc
    public static final String DOCUMENT_WIRE = "{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}";
    public static final String DOCUMENT_REF_WIRE  = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"}}}";
    public static final Document DOCUMENT = new Document(
            "123",
            new Module("Foo"),
            Instant.parse("2023-12-15T01:01:01.0010010Z"),
            Map.of("first_name","foo", "last_name", "bar","age",42)
    );
    
    // Named doc
    public static final String NAMED_DOCUMENT_WIRE = "{\"@doc\":{\"name\":\"Boogles\",\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"coll\":{\"@mod\":\"Foo\"},\"first_name\":\"foo\",\"last_name\":\"bar\"}}";
    public static final String NAMED_DOCUMENT_REF_WIRE = "{\"@ref\":{\"name\":\"Boogles\",\"coll\":{\"@mod\":\"Foo\"}}}";
    public static final NamedDocument NAMED_DOCUMENT = new NamedDocument(
            "Boogles",
            new Module("Foo"),
            Instant.parse("2023-12-15T01:01:01.0010010Z"),
            Map.of("first_name","foo", "last_name", "bar")
    );
    
    // Null doc
    public static final String NULL_DOC_WIRE = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"exists\":false,\"cause\":\"not found\"}}";
    public static final NullDocument<Document> NULL_DOCUMENT = new NullDocument<>("123", new Module("Foo"), "not found");

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.Decode, NULLABLE_DOC_CODEC, DOCUMENT_WIRE, new NonNullDocument<>(DOCUMENT), null),
                Arguments.of(TestType.Encode, NULLABLE_DOC_CODEC, DOCUMENT_REF_WIRE, new NonNullDocument<>(DOCUMENT), null),
                Arguments.of(TestType.Decode, NULLABLE_DOC_CODEC, NAMED_DOCUMENT_WIRE, new NonNullDocument<>(NAMED_DOCUMENT), null),
                Arguments.of(TestType.Encode, NULLABLE_DOC_CODEC, NAMED_DOCUMENT_REF_WIRE, new NonNullDocument<>(NAMED_DOCUMENT), null),
                Arguments.of(TestType.Decode, NULLABLE_DOC_CODEC, NULL_DOC_WIRE, NULL_DOCUMENT, null),
                Arguments.of(TestType.Decode, NULLABLE_ClASS_CODEC, DOCUMENT_WIRE, new NonNullDocument<>(CLASS_WITH_ATTRIBUTES), null),
                Arguments.of(TestType.Encode, NULLABLE_ClASS_CODEC, CLASS_WITH_ATTRIBUTES_WIRE, new NonNullDocument<>(CLASS_WITH_ATTRIBUTES), null),
                Arguments.of(TestType.Decode, NULLABLE_ClASS_CODEC, NULL_DOC_WIRE, NULL_DOCUMENT, null)
        );
    }

    @ParameterizedTest(name = "NullableCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void nullable_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(NULLABLE_DOC_CODEC);
    }

    @ParameterizedTest(name = "NullableCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void nullable_runUnsupportedTypeTestCases(String wire, FaunaType type) throws IOException {
        // This codec will pass through the supported types of the underlying codec
        var exMsg = MessageFormat.format("Unable to decode `{0}` with `BaseDocumentCodec<BaseDocument>`. Supported types for codec are [Document, Null, Ref].", type);
        runCase(TestType.Decode, NULLABLE_DOC_CODEC, wire, null, new ClientException(exMsg));
    }
}
