package com.fauna.codec.codecs;

import com.fauna.codec.*;
import com.fauna.exception.ClientException;
import com.fauna.types.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;

public class CodecTests {

    public enum TestType {
        RoundTrip,
        Decode,
        Encode
    }

    private static Stream<Arguments> testArgs() {
        return Stream.of(
                // StringCodec
                Arguments.of(TestType.RoundTrip, STRING_CODEC, STRING_WIRE, FAUNA, null),
                Arguments.of(TestType.RoundTrip, STRING_CODEC, NULL_WIRE, null, null),
                Arguments.of(TestType.Decode, STRING_CODEC, BYTES_WIRE, BASE64_STRING, null),

                // ByteCodec
                Arguments.of(TestType.RoundTrip, BYTE_CODEC, MAX_BYTE_WIRE, Byte.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, BYTE_CODEC, MIN_BYTE_WIRE, Byte.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, BYTE_CODEC, NULL_WIRE, null, null),

                // ShortCodec
                Arguments.of(TestType.RoundTrip, SHORT_CODEC, MAX_SHORT_WIRE, Short.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, SHORT_CODEC, MIN_SHORT_WIRE, Short.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, SHORT_CODEC, NULL_WIRE, null, null),

                // IntCodec
                Arguments.of(TestType.RoundTrip, INT_CODEC, MAX_INT_WIRE, Integer.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, INT_CODEC, MIN_INT_WIRE, Integer.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, INT_CODEC, NULL_WIRE, null, null),

                // LongCodec
                Arguments.of(TestType.RoundTrip, LONG_CODEC, MAX_LONG_WIRE, Long.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, LONG_CODEC, MIN_LONG_WIRE, Long.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, LONG_CODEC, NULL_WIRE, null, null),

                // FloatCodec
                Arguments.of(TestType.RoundTrip, FLOAT_CODEC, MAX_FLOAT_WIRE, Float.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, FLOAT_CODEC, MIN_FLOAT_WIRE, Float.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, FLOAT_CODEC, NULL_WIRE, null, null),

                // DoubleCodec
                Arguments.of(TestType.RoundTrip, DOUBLE_CODEC, MAX_DOUBLE_WIRE, Double.MAX_VALUE, null),
                Arguments.of(TestType.RoundTrip, DOUBLE_CODEC, MIN_DOUBLE_WIRE, Double.MIN_VALUE, null),
                Arguments.of(TestType.RoundTrip, DOUBLE_CODEC, NULL_WIRE, null, null),

                // CharCodec
                Arguments.of(TestType.RoundTrip, CHAR_CODEC, "{\"@int\":\"84\"}", 'T', null),
                Arguments.of(TestType.RoundTrip, CHAR_CODEC, NULL_WIRE, null, null),

                // BoolCodec
                Arguments.of(TestType.RoundTrip, BOOL_CODEC, TRUE_WIRE, true, null),
                Arguments.of(TestType.RoundTrip, BOOL_CODEC, FALSE_WIRE, false, null),
                Arguments.of(TestType.RoundTrip, BOOL_CODEC, NULL_WIRE, null, null),

                // InstantCodec
                Arguments.of(TestType.RoundTrip, INSTANT_CODEC, INSTANT_UTC_WIRE, INSTANT, null),
                Arguments.of(TestType.RoundTrip, INSTANT_CODEC, NULL_WIRE, null, null),
                Arguments.of(TestType.Decode, INSTANT_CODEC, INSTANT_PACIFIC_WIRE, INSTANT, null),
                Arguments.of(TestType.Encode, INSTANT_CODEC, INSTANT_UTC_WIRE, INSTANT, null),

                // LocalDateCodec
                Arguments.of(TestType.RoundTrip, LOCAL_DATE_CODEC, DATE_WIRE, DATE, null),
                Arguments.of(TestType.RoundTrip, LOCAL_DATE_CODEC, NULL_WIRE, null, null),

                // ModuleCodec
                Arguments.of(TestType.RoundTrip, MODULE_CODEC, MODULE_WIRE, MODULE, null),
                Arguments.of(TestType.RoundTrip, MODULE_CODEC, NULL_WIRE, null, null),

                // ByteArrayCodec
                Arguments.of(TestType.RoundTrip, BYTE_ARRAY_CODEC, BYTES_WIRE, new byte[]{70, 97, 117, 110, 97}, null),
                Arguments.of(TestType.RoundTrip, BYTE_ARRAY_CODEC, NULL_WIRE, null, null),

                // ListCodec
                Arguments.of(TestType.RoundTrip, LIST_INT_CODEC, ARRAY_WIRE, List.of(42), null),
                Arguments.of(TestType.RoundTrip, LIST_INT_CODEC, NULL_WIRE, null, null),

                // MapCodec
                Arguments.of(TestType.RoundTrip, MAP_INT_CODEC, OBJECT_WIRE, Map.of("key1", 42), null),

                // ClassCodec
                Arguments.of(TestType.RoundTrip, CLASS_WITH_PARAMETERIZED_FIELDS_CODEC, CLASS_WITH_PARAMETERIZED_FIELDS_WIRE, CLASS_WITH_PARAMETERIZED_FIELDS, null),
                Arguments.of(TestType.RoundTrip, CLASS_WITH_REF_TAG_COLLISION_CODEC, ESCAPED_OBJECT_WITH_WIRE("@ref"), CLASS_WITH_REF_TAG_COLLISION, null),
                Arguments.of(TestType.Decode, PERSON_WITH_ATTRIBUTES_CODEC, DOCUMENT_WIRE, PERSON_WITH_ATTRIBUTES, null),
                Arguments.of(TestType.Decode, PERSON_WITH_ATTRIBUTES_CODEC, NULL_DOC_WIRE, null, NULL_DOC_EXCEPTION),

                // OptionalCodec
                Arguments.of(TestType.RoundTrip, OPTIONAL_INT_CODEC, NULL_WIRE, Optional.empty(), null),
                Arguments.of(TestType.RoundTrip, OPTIONAL_STRING_CODEC, STRING_WIRE, Optional.of(FAUNA), null),

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
                Arguments.of(TestType.RoundTrip, BASE_REF_CODEC, DOCUMENT_REF_WIRE, DOCUMENT_REF, null),
                Arguments.of(TestType.RoundTrip, BASE_REF_CODEC, NAMED_DOCUMENT_REF_WIRE, NAMED_DOCUMENT_REF, null),
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
                if (obj instanceof byte[]) {
                    assertArrayEquals((byte[]) obj, (byte[]) decodeRoundTrip);
                } else {
                    assertEquals(obj, decodeRoundTrip);
                }
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
        all_codecs(TestType.RoundTrip, MAP_STRING_CODEC, ESCAPED_OBJECT_WITH_WIRE(tag), Map.of(tag, "not"), null);
    }
}
