package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithClientGeneratedIdCollTsAnnotations;
import com.fauna.beans.ClassWithInheritanceL2;
import com.fauna.beans.ClassWithFaunaIgnore;
import com.fauna.beans.ClassWithIdCollTsAnnotations;
import com.fauna.beans.ClassWithParameterizedFields;
import com.fauna.beans.ClassWithRefTagCollision;
import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.FaunaType;
import com.fauna.exception.CodecException;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.Module;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.ESCAPED_OBJECT_WIRE_WITH;


public class ClassCodecTest extends TestBase {

    // Class with FaunaField attributes
    public static final Codec<ClassWithAttributes> CLASS_WITH_ATTRIBUTES_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithAttributes.class);
    public static final String DOCUMENT_WIRE = "{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}";
    public static final ClassWithAttributes CLASS_WITH_ATTRIBUTES = new ClassWithAttributes("foo","bar",42);
    public static final String NULL_DOC_WIRE = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"exists\":false,\"cause\":\"not found\"}}";
    public static final NullDocumentException NULL_DOC_EXCEPTION = new NullDocumentException("123", new Module("Foo"), "not found");

    // Class with tag collision
    public static final Codec<ClassWithRefTagCollision> CLASS_WITH_REF_TAG_COLLISION_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithRefTagCollision.class);
    public static final String REF_TAG_COLLISION_WIRE = ESCAPED_OBJECT_WIRE_WITH("@ref");
    public static final ClassWithRefTagCollision CLASS_WITH_REF_TAG_COLLISION = new ClassWithRefTagCollision("not");

    // Class with parameterized Fields
    public static final Codec<ClassWithParameterizedFields>  CLASS_WITH_PARAMETERIZED_FIELDS_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithParameterizedFields.class);
    public static final String CLASS_WITH_PARAMETERIZED_FIELDS_WIRE = "{\"first_name\":\"foo\",\"a_list\":[{\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}],\"a_map\":{\"key1\":{\"@int\":\"42\"}},\"an_optional\":\"Fauna\"}";
    public static final ClassWithParameterizedFields CLASS_WITH_PARAMETERIZED_FIELDS = new ClassWithParameterizedFields("foo",  List.of(CLASS_WITH_ATTRIBUTES), Map.of("key1", 42), Optional.of("Fauna"));

    // Class with FaunaIgnore attributes
    public static final Codec<ClassWithFaunaIgnore> CLASS_WITH_FAUNA_IGNORE_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithFaunaIgnore.class);
    public static final String CLASS_WITH_FAUNA_IGNORE_WITH_AGE_WIRE = "{\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}";
    public static final ClassWithFaunaIgnore CLASS_WITH_FAUNA_IGNORE_WITH_AGE = new ClassWithFaunaIgnore("foo", "bar", 42);
    public static final String CLASS_WITH_FAUNA_IGNORE_WIRE = "{\"first_name\":\"foo\",\"last_name\":\"bar\"}";
    public static final ClassWithFaunaIgnore CLASS_WITH_FAUNA_IGNORE = new ClassWithFaunaIgnore("foo", "bar", null);

    // Class with Id, Coll, Ts annotations
    private static final Object CLASS_WITH_ID_COLL_TS_ANNOTATIONS_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithIdCollTsAnnotations.class);
    private static final String CLASS_WITH_ID_COLL_TS_ANNOTATIONS_WIRE =  "{\"firstName\":\"foo\",\"lastName\":\"bar\"}";
    private static final ClassWithIdCollTsAnnotations CLASS_WITH_ID_COLL_TS_ANNOTATIONS = new ClassWithIdCollTsAnnotations("123", new Module("mod"), Instant.parse("2024-01-23T13:33:10.300Z"), "foo", "bar");


    // Class with Client Generated Id, Coll, Ts annotations
    private static final Object CLASS_WITH_CLIENT_GENERATED_ID_COLL_TS_ANNOTATIONS_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithClientGeneratedIdCollTsAnnotations.class);
    private static final String CLASS_WITH_CLIENT_GENERATED_ID_COLL_TS_ANNOTATIONS_WIRE =  "{\"id\":\"123\",\"firstName\":\"foo\",\"lastName\":\"bar\"}";
    private static final ClassWithClientGeneratedIdCollTsAnnotations CLASS_WITH_CLIENT_GENERATED_ID_COLL_TS_ANNOTATIONS = new ClassWithClientGeneratedIdCollTsAnnotations("123", new Module("mod"), Instant.parse("2024-01-23T13:33:10.300Z"), "foo", "bar");
    private static final ClassWithClientGeneratedIdCollTsAnnotations CLASS_WITH_CLIENT_GENERATED_ID_NULL_ANNOTATIONS = new ClassWithClientGeneratedIdCollTsAnnotations(null, new Module("mod"), Instant.parse("2024-01-23T13:33:10.300Z"), "foo", "bar");


    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, CLASS_WITH_PARAMETERIZED_FIELDS_CODEC, CLASS_WITH_PARAMETERIZED_FIELDS_WIRE, CLASS_WITH_PARAMETERIZED_FIELDS, null),
                Arguments.of(TestType.RoundTrip, CLASS_WITH_REF_TAG_COLLISION_CODEC, REF_TAG_COLLISION_WIRE, CLASS_WITH_REF_TAG_COLLISION, null),
                Arguments.of(TestType.RoundTrip, CLASS_WITH_FAUNA_IGNORE_CODEC, CLASS_WITH_FAUNA_IGNORE_WIRE, CLASS_WITH_FAUNA_IGNORE, null),
                Arguments.of(TestType.Encode, CLASS_WITH_FAUNA_IGNORE_CODEC, CLASS_WITH_FAUNA_IGNORE_WIRE, CLASS_WITH_FAUNA_IGNORE_WITH_AGE, null),
                Arguments.of(TestType.Decode, CLASS_WITH_FAUNA_IGNORE_CODEC, CLASS_WITH_FAUNA_IGNORE_WITH_AGE_WIRE, CLASS_WITH_FAUNA_IGNORE, null),
                Arguments.of(TestType.Decode, CLASS_WITH_ATTRIBUTES_CODEC, DOCUMENT_WIRE, CLASS_WITH_ATTRIBUTES, null),
                Arguments.of(TestType.Decode, CLASS_WITH_ATTRIBUTES_CODEC, NULL_DOC_WIRE, null, NULL_DOC_EXCEPTION),
                Arguments.of(TestType.Encode, CLASS_WITH_ID_COLL_TS_ANNOTATIONS_CODEC, CLASS_WITH_ID_COLL_TS_ANNOTATIONS_WIRE, CLASS_WITH_ID_COLL_TS_ANNOTATIONS, null),
                Arguments.of(TestType.Encode, CLASS_WITH_CLIENT_GENERATED_ID_COLL_TS_ANNOTATIONS_CODEC, CLASS_WITH_CLIENT_GENERATED_ID_COLL_TS_ANNOTATIONS_WIRE, CLASS_WITH_CLIENT_GENERATED_ID_COLL_TS_ANNOTATIONS, null),
                Arguments.of(TestType.Encode, CLASS_WITH_CLIENT_GENERATED_ID_COLL_TS_ANNOTATIONS_CODEC, CLASS_WITH_ID_COLL_TS_ANNOTATIONS_WIRE, CLASS_WITH_CLIENT_GENERATED_ID_NULL_ANNOTATIONS, null)
        );
    }

    @ParameterizedTest(name = "ClassCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void class_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);
    }
    @Test
    public void class_roundTripWithInheritance() throws IOException {
        var codec = DefaultCodecProvider.SINGLETON.get(ClassWithInheritanceL2.class);
        var wire = "{\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}";
        var obj = new ClassWithInheritanceL2("foo","bar",42);
        runCase(TestType.RoundTrip, codec, wire, obj, null);
    }

    public static Stream<Arguments> unsupportedTypeCases() {
        return unsupportedTypeCases(CLASS_WITH_ATTRIBUTES_CODEC);
    }

    @ParameterizedTest(name = "ClassCodecUnsupportedTypes({index}) -> {0}:{1}")
    @MethodSource("unsupportedTypeCases")
    public void class_runUnsupportedTypeTestCases(String wire, FaunaType type) throws IOException {
        var exMsg = MessageFormat.format("Unable to decode `{0}` with `ClassCodec<ClassWithAttributes>`. Supported types for codec are [Document, Null, Object, Ref].", type);
        runCase(TestType.Decode, CLASS_WITH_ATTRIBUTES_CODEC, wire, null, new CodecException(exMsg));
    }
}
