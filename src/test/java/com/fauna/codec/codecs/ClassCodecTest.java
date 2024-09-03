package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithFaunaIgnore;
import com.fauna.beans.ClassWithOptionalFields;
import com.fauna.beans.ClassWithParameterizedFields;
import com.fauna.beans.ClassWithRefTagCollision;
import com.fauna.beans.ClassWithAttributes;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.Module;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.fauna.codec.codecs.Fixtures.ESCAPED_OBJECT_WIRE_WITH;


public class ClassCodecTest extends TestBase {

    // Class with tag collision
    public static final Codec<ClassWithRefTagCollision> CLASS_WITH_REF_TAG_COLLISION_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithRefTagCollision.class);
    public static final String REF_TAG_COLLISION_WIRE = ESCAPED_OBJECT_WIRE_WITH("@ref");
    public static final ClassWithRefTagCollision CLASS_WITH_REF_TAG_COLLISION = new ClassWithRefTagCollision("not");

    // Class with parameterized Fields
    public static final Codec<ClassWithParameterizedFields>  CLASS_WITH_PARAMETERIZED_FIELDS_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithParameterizedFields.class);
    public static final String CLASS_WITH_PARAMETERIZED_FIELDS_WIRE = "{\"first_name\":\"foo\",\"a_list\":[\"item1\"],\"a_map\":{\"key1\":{\"@int\":\"42\"}}}";
    public static final ClassWithParameterizedFields CLASS_WITH_PARAMETERIZED_FIELDS = new ClassWithParameterizedFields("foo",  List.of("item1"), Map.of("key1", 42));

    // Class with FaunaField attributes
    public static final Codec<ClassWithAttributes> CLASS_WITH_ATTRIBUTES_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithAttributes.class);
    public static final String DOCUMENT_WIRE = "{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}";
    public static final ClassWithAttributes CLASS_WITH_ATTRIBUTES = new ClassWithAttributes("foo","bar",42);
    public static final String NULL_DOC_WIRE = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"exists\":false,\"cause\":\"not found\"}}";
    public static final NullDocumentException NULL_DOC_EXCEPTION = new NullDocumentException("123", new Module("Foo"), "not found");


    // Class with FaunaIgnore attributes
    public static final Codec<ClassWithFaunaIgnore> CLASS_WITH_FAUNA_IGNORE_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithFaunaIgnore.class);
    public static String CLASS_WITH_FAUNA_IGNORE_WITH_AGE_WIRE = "{\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}";
    public static final ClassWithFaunaIgnore CLASS_WITH_FAUNA_IGNORE_WITH_AGE = new ClassWithFaunaIgnore("foo", "bar", 42);
    public static String CLASS_WITH_FAUNA_IGNORE_WIRE = "{\"first_name\":\"foo\",\"last_name\":\"bar\"}";
    public static final ClassWithFaunaIgnore CLASS_WITH_FAUNA_IGNORE = new ClassWithFaunaIgnore("foo", "bar", null);

    // Class with Optional fields
    private static final Object CLASS_WITH_OPTIONAL_FIELDS_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithOptionalFields.class);
    private static final String CLASS_WITH_OPTIONAL_FIELDS_NON_NULL_WIRE =  "{\"firstName\":\"foo\",\"lastName\":\"bar\"}";
    private static final ClassWithOptionalFields CLASS_WITH_OPTIONAL_NON_NULL_FIELDS = new ClassWithOptionalFields("foo", Optional.of("bar"));
    private static final String CLASS_WITH_OPTIONAL_FIELDS_EMPTY_WIRE =  "{\"firstName\":\"foo\",\"lastName\":null}";
    private static final ClassWithOptionalFields CLASS_WITH_OPTIONAL_EMPTY_FIELDS = new ClassWithOptionalFields("foo", Optional.empty());
    private static final String CLASS_WITH_OPTIONAL_FIELDS_NULL_WIRE =  "{\"firstName\":\"foo\"}";
    private static final ClassWithOptionalFields CLASS_WITH_OPTIONAL_NULL_FIELDS = new ClassWithOptionalFields("foo", null);

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(TestType.RoundTrip, CLASS_WITH_PARAMETERIZED_FIELDS_CODEC, CLASS_WITH_PARAMETERIZED_FIELDS_WIRE, CLASS_WITH_PARAMETERIZED_FIELDS, null),
                Arguments.of(TestType.RoundTrip, CLASS_WITH_REF_TAG_COLLISION_CODEC, REF_TAG_COLLISION_WIRE, CLASS_WITH_REF_TAG_COLLISION, null),
                Arguments.of(TestType.RoundTrip, CLASS_WITH_FAUNA_IGNORE_CODEC, CLASS_WITH_FAUNA_IGNORE_WIRE, CLASS_WITH_FAUNA_IGNORE, null),
                Arguments.of(TestType.Encode, CLASS_WITH_FAUNA_IGNORE_CODEC, CLASS_WITH_FAUNA_IGNORE_WIRE, CLASS_WITH_FAUNA_IGNORE_WITH_AGE, null),
                Arguments.of(TestType.Decode, CLASS_WITH_FAUNA_IGNORE_CODEC, CLASS_WITH_FAUNA_IGNORE_WITH_AGE_WIRE, CLASS_WITH_FAUNA_IGNORE, null),
                Arguments.of(TestType.Decode, CLASS_WITH_ATTRIBUTES_CODEC, DOCUMENT_WIRE, CLASS_WITH_ATTRIBUTES, null),
                Arguments.of(TestType.Decode, CLASS_WITH_ATTRIBUTES_CODEC, NULL_DOC_WIRE, null, NULL_DOC_EXCEPTION),
                Arguments.of(TestType.RoundTrip, CLASS_WITH_OPTIONAL_FIELDS_CODEC, CLASS_WITH_OPTIONAL_FIELDS_NON_NULL_WIRE, CLASS_WITH_OPTIONAL_NON_NULL_FIELDS, null),
                Arguments.of(TestType.RoundTrip, CLASS_WITH_OPTIONAL_FIELDS_CODEC, CLASS_WITH_OPTIONAL_FIELDS_NULL_WIRE, CLASS_WITH_OPTIONAL_NULL_FIELDS, null),
                Arguments.of(TestType.RoundTrip, CLASS_WITH_OPTIONAL_FIELDS_CODEC, CLASS_WITH_OPTIONAL_FIELDS_EMPTY_WIRE, CLASS_WITH_OPTIONAL_EMPTY_FIELDS, null)
        );
    }

    @ParameterizedTest(name = "ClassCodec({index}) -> {0}:{1}:{2}:{3}:{4}")
    @MethodSource("testCases")
    public <T,E extends Exception> void class_runTestCases(TestType testType, Codec<T> codec, String wire, Object obj, E exception) throws IOException {
        runCase(testType, codec, wire, obj, exception);

    }
}
