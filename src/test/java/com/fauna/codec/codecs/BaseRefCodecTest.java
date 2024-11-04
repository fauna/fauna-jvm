package com.fauna.codec.codecs;

import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.BaseRef;
import com.fauna.types.DocumentRef;
import com.fauna.types.Module;
import com.fauna.types.NamedDocumentRef;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.stream.Stream;


public class BaseRefCodecTest extends TestBase {
    public static final Codec<BaseRef> BASE_REF_CODEC =
            DefaultCodecProvider.SINGLETON.get(BaseRef.class);

    // Doc ref
    public static final String DOCUMENT_REF_WIRE =
            "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"}}}";
    public static final DocumentRef DOCUMENT_REF =
            new DocumentRef("123", new Module("Foo"));

    // Named ref
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
                Arguments.of(TestType.RoundTrip, BASE_REF_CODEC,
                        DOCUMENT_REF_WIRE, DOCUMENT_REF, null),
                Arguments.of(TestType.RoundTrip, BASE_REF_CODEC,
                        NAMED_DOCUMENT_REF_WIRE, NAMED_DOCUMENT_REF, null),
                Arguments.of(TestType.Decode, BASE_REF_CODEC, NULL_DOC_WIRE,
                        null, NULL_DOC_EXCEPTION)

        );
    }

    @ParameterizedTest(name = "BaseRefCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T, E extends Exception> void baseRef_runTestCases(TestType testType,
                                                              Codec<T> codec,
                                                              String wire,
                                                              Object obj,
                                                              E exception)
            throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(BASE_REF_CODEC);
    }

    @ParameterizedTest(name = "BaseRefCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void baseRef_runUnsupportedTypeTestCases(String wire, FaunaType type)
            throws IOException {
        var exMsg = MessageFormat.format(
                "Unable to decode `{0}` with `BaseRefCodec<BaseRef>`. Supported types for codec are [Null, Ref].",
                type);
        runCase(TestType.Decode, BASE_REF_CODEC, wire, null,
                new CodecException(exMsg));
    }
}
