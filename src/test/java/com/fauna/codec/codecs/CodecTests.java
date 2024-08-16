package com.fauna.codec.codecs;

import com.fauna.codec.*;
import com.fauna.exception.ClientException;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.Document;
import com.fauna.types.Module;
import com.fauna.types.NonNull;
import com.fauna.types.NullDoc;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CodecTests {

    public enum TestType {
        RoundTrip,
        Decode,
        Encode
    }

    private static Stream<Arguments> testArgs() {
        return Stream.of(
                // StringCodec
                Arguments.of(TestType.RoundTrip, STRING_CODEC, STRING_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, STRING_CODEC, NULL_WIRE, null, null),
                Arguments.of(TestType.Decode, STRING_CODEC, BYTES_WIRE, BASE64_STRING, null),

                // ByteCodec
                Arguments.of(TestType.RoundTrip, BYTE_CODEC, MAX_BYTE_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, BYTE_CODEC, MIN_BYTE_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, BYTE_CODEC, NULL_WIRE, null, null),

                // ShortCodec
                Arguments.of(TestType.RoundTrip, SHORT_CODEC, MAX_SHORT_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, SHORT_CODEC, MIN_SHORT_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, SHORT_CODEC, NULL_WIRE, null, null),

                // IntCodec
                Arguments.of(TestType.RoundTrip, INT_CODEC, MAX_INT_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, INT_CODEC, MIN_INT_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, INT_CODEC, NULL_WIRE, null, null),

                // LongCodec
                Arguments.of(TestType.RoundTrip, LONG_CODEC, MAX_LONG_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, LONG_CODEC, MIN_LONG_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, LONG_CODEC, NULL_WIRE, null, null),

                // FloatCodec
                Arguments.of(TestType.RoundTrip, FLOAT_CODEC, MAX_FLOAT_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, FLOAT_CODEC, MIN_FLOAT_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, FLOAT_CODEC, NULL_WIRE, null, null),

                // DoubleCodec
                Arguments.of(TestType.RoundTrip, DOUBLE_CODEC, MAX_DOUBLE_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, DOUBLE_CODEC, MIN_DOUBLE_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, DOUBLE_CODEC, NULL_WIRE, null, null),

                // CharCodec
                Arguments.of(TestType.RoundTrip, LONG_CODEC, MAX_LONG_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, CHAR_CODEC, NULL_WIRE, null, null),

                // BoolCodec
                Arguments.of(TestType.RoundTrip, BOOL_CODEC, TRUE_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, BOOL_CODEC, FALSE_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, BOOL_CODEC, NULL_WIRE, null, null),

                // ByteArrayCodec
                Arguments.of(TestType.RoundTrip, BYTE_ARRAY_CODEC, BYTES_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, BYTE_ARRAY_CODEC, NULL_WIRE, null, null),

                // ListCodec
                Arguments.of(TestType.RoundTrip, LIST_INT_CODEC, ARRAY_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, LIST_INT_CODEC, NULL_WIRE, null, null),

                // MapCodec
                Arguments.of(TestType.RoundTrip, MAP_INT_CODEC, OBJECT_WIRE, null, null),

                // ClassCodec
                Arguments.of(TestType.RoundTrip, CLASS_WITH_PARAMETERIZED_FIELDS_CODEC, CLASS_WITH_PARAMETERIZED_FIELDS_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, CLASS_WITH_REF_TAG_COLLISION_CODEC, ESCAPED_OBJECT_WITH_WIRE("@ref"), null, null),
                Arguments.of(TestType.Decode, PERSON_WITH_ATTRIBUTES_CODEC, DOCUMENT_WIRE, PERSON_WITH_ATTRIBUTES, null),
                Arguments.of(TestType.Decode, PERSON_WITH_ATTRIBUTES_CODEC, NULL_DOC_WIRE, null, NULL_DOC_EXCEPTION),

                // OptionalCodec
                Arguments.of(TestType.RoundTrip, OPTIONAL_INT_CODEC, NULL_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, OPTIONAL_STRING_CODEC, STRING_WIRE, null, null),

                // PageCodec
                Arguments.of(TestType.RoundTrip, PAGE_CODEC, NULL_WIRE, null, null),
                Arguments.of(TestType.Decode, PAGE_CODEC, PAGE_WIRE, PAGE_DOCUMENT, null),
                Arguments.of(TestType.Decode, PAGE_CODEC, DOCUMENT_WIRE, PAGE_DOCUMENT, null),

                // BaseDocumentCodec
                Arguments.of(TestType.Decode, BASE_DOCUMENT_CODEC, DOCUMENT_WIRE, DOCUMENT, null),
                Arguments.of(TestType.Decode, BASE_DOCUMENT_CODEC, NAMED_DOCUMENT_WIRE, NAMED_DOCUMENT, null),
                Arguments.of(TestType.Decode, BASE_DOCUMENT_CODEC, NULL_DOC_WIRE, null, NULL_DOC_EXCEPTION),
                Arguments.of(TestType.Decode, BASE_DOCUMENT_CODEC, DOCUMENT_REF_WIRE, null, new ClientException("Unexpected type `class com.fauna.types.DocumentRef` decoding with `class com.fauna.codec.codecs.BaseDocumentCodec`")),
                Arguments.of(TestType.Encode, BASE_DOCUMENT_CODEC, DOCUMENT_REF_WIRE, DOCUMENT, null),
                Arguments.of(TestType.Encode, BASE_DOCUMENT_CODEC, NAMED_DOCUMENT_REF_WIRE, NAMED_DOCUMENT, null),

                // BaseRefCodec
                Arguments.of(TestType.RoundTrip, BASE_REF_CODEC, DOCUMENT_REF_WIRE, null, null),
                Arguments.of(TestType.RoundTrip, BASE_REF_CODEC, NAMED_DOCUMENT_REF_WIRE, null, null),
                Arguments.of(TestType.Decode, BASE_REF_CODEC, NULL_DOC_WIRE, null, NULL_DOC_EXCEPTION),

                // NullableCodec
                Arguments.of(TestType.Decode, NULLABLE_DOC_CODEC, DOCUMENT_WIRE, new NonNull<>(DOCUMENT), null),
                Arguments.of(TestType.Encode, NULLABLE_DOC_CODEC, DOCUMENT_REF_WIRE, new NonNull<>(DOCUMENT), null),
                Arguments.of(TestType.Decode, NULLABLE_DOC_CODEC, NAMED_DOCUMENT_WIRE, new NonNull<>(NAMED_DOCUMENT), null),
                Arguments.of(TestType.Encode, NULLABLE_DOC_CODEC, NAMED_DOCUMENT_REF_WIRE, new NonNull<>(NAMED_DOCUMENT), null),
                Arguments.of(TestType.Decode, NULLABLE_DOC_CODEC, NULL_DOC_WIRE, NULL_DOCUMENT, null),
                Arguments.of(TestType.Decode, NULLABLE_PERSON_CODEC, DOCUMENT_WIRE, new NonNull<>(PERSON_WITH_ATTRIBUTES), null),
                Arguments.of(TestType.Encode, NULLABLE_PERSON_CODEC, PERSON_WITH_ATTRIBUTES_WIRE, new NonNull<>(PERSON_WITH_ATTRIBUTES), null),
                Arguments.of(TestType.Decode, NULLABLE_PERSON_CODEC, NULL_DOC_WIRE, NULL_DOCUMENT, null),

                // DynamicCodec
                Arguments.of(TestType.Decode, DYNAMIC_CODEC, DOCUMENT_WIRE, DOCUMENT, null),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC, DOCUMENT_REF_WIRE, DOCUMENT_REF, null),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC, NAMED_DOCUMENT_WIRE, NAMED_DOCUMENT, null),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC, NAMED_DOCUMENT_REF_WIRE, NAMED_DOCUMENT_REF, null),
                Arguments.of(TestType.Decode, DYNAMIC_CODEC, NULL_DOC_WIRE, null, NULL_DOC_EXCEPTION)

        );
    }

    @ParameterizedTest(name = "{index} {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testArgs")
    public <T,E extends Exception> void all_codecs(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        switch (testType) {
            case RoundTrip:
                var decodeRoundTrip = Helpers.decode(codec, wire);
                var encodeRoundTrip = Helpers.encode(codec, decodeRoundTrip);
                assertEquals(wire, encodeRoundTrip);
                break;
            case Decode:
                if (exception != null) {
                    var ex = assertThrows(exception.getClass(), () -> {
                        Helpers.decode(codec, wire);
                    });

                    assertEquals(exception.getMessage(), ex.getMessage());
                } else {
                    var decoded = Helpers.decode(codec, wire);
                    assertEquals(obj, decoded);
                }
                break;
            case Encode:
                if (exception != null) {
                    var ex = assertThrows(exception.getClass(), () -> {
                        Helpers.encode(codec, (T) obj);
                    });

                    assertEquals(exception.getMessage(), ex.getMessage());
                } else {
                    var encoded = Helpers.encode(codec, (T) obj);
                    assertEquals(wire, encoded);
                }
        }
    }

    private static Set<String> tags() {
        return BaseCodec.TAGS;
    }

    @ParameterizedTest
    @MethodSource("tags")
    public void map_escapeOnReservedKey(String tag) throws IOException {
        all_codecs(TestType.RoundTrip, MAP_STRING_CODEC, ESCAPED_OBJECT_WITH_WIRE(tag), null, null);
    }
}
