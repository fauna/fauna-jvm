package com.fauna.codec.codecs;

import com.fauna.beans.ClassWithParameterizedFields;
import com.fauna.beans.ClassWithRefTagCollision;
import com.fauna.beans.PersonWithAttributes;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.NullDocumentException;
import com.fauna.types.*;
import com.fauna.types.Module;

import java.time.Instant;
import java.util.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class Fixtures {

    // Wire Strings
    public static final String NULL_WIRE = "null";
    public static final String STRING_WIRE = "\"Fauna\"";
    public static final String TRUE_WIRE = "true";
    public static final String FALSE_WIRE = "false";
    public static final String BYTES_WIRE = "{\"@bytes\":\"RmF1bmE=\"}";
    public static final String BASE64_STRING = "RmF1bmE="; // Fauna
    public static final String MAX_BYTE_WIRE = String.format("{\"@int\":\"%s\"}", Byte.MAX_VALUE);
    public static final String MIN_BYTE_WIRE = String.format("{\"@int\":\"%s\"}", Byte.MIN_VALUE);
    public static final String MAX_SHORT_WIRE = String.format("{\"@int\":\"%s\"}", Short.MAX_VALUE);
    public static final String MIN_SHORT_WIRE = String.format("{\"@int\":\"%s\"}", Short.MIN_VALUE);
    public static final String MAX_INT_WIRE = String.format("{\"@int\":\"%s\"}", Integer.MAX_VALUE);
    public static final String MIN_INT_WIRE = String.format("{\"@int\":\"%s\"}", Integer.MIN_VALUE);
    public static final String MAX_LONG_WIRE = String.format("{\"@long\":\"%s\"}", Long.MAX_VALUE);
    public static final String MIN_LONG_WIRE = String.format("{\"@long\":\"%s\"}", Long.MIN_VALUE);
    public static final String MAX_FLOAT_WIRE = String.format("{\"@double\":\"%s\"}", Float.MAX_VALUE);
    public static final String MIN_FLOAT_WIRE = String.format("{\"@double\":\"%s\"}", Float.MIN_VALUE);
    public static final String MAX_DOUBLE_WIRE = String.format("{\"@double\":\"%s\"}", Double.MAX_VALUE);
    public static final String MIN_DOUBLE_WIRE = String.format("{\"@double\":\"%s\"}", Double.MIN_VALUE);

    public static final String OBJECT_WIRE = "{\"key1\":{\"@int\":\"42\"}}";
    public static String ESCAPED_OBJECT_WITH_WIRE(String tag) {
        return String.format("{\"@object\":{\"%s\":\"not\"}}", tag);
    }
    public static final String ARRAY_WIRE = "[{\"@int\":\"42\"}]";
    public static final String CLASS_WITH_PARAMETERIZED_FIELDS_WIRE = "{\"first_name\":\"foo\",\"a_list\":[\"item1\"],\"a_map\":{\"key1\":{\"@int\":\"42\"}}}";
    public static final String DOCUMENT_WIRE = "{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}";
    public static final String DOCUMENT_REF_WIRE  = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"}}}";
    public static final String NAMED_DOCUMENT_WIRE = "{\"@doc\":{\"name\":\"Boogles\",\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"coll\":{\"@mod\":\"Foo\"},\"first_name\":\"foo\",\"last_name\":\"bar\"}}";
    public static final String NAMED_DOCUMENT_REF_WIRE = "{\"@ref\":{\"name\":\"Boogles\",\"coll\":{\"@mod\":\"Foo\"}}}";
    public static final String NULL_DOC_WIRE = "{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"exists\":false,\"cause\":\"not found\"}}";
    public static final String PAGE_WIRE = "{\"@set\":{\"data\":[{\"@doc\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Foo\"},\"ts\":{\"@time\":\"2023-12-15T01:01:01.0010010Z\"},\"first_name\":\"foo\",\"last_name\":\"bar\",\"age\":{\"@int\":\"42\"}}}],\"after\": null}}";


    // Objects
    public static final PersonWithAttributes PERSON_WITH_ATTRIBUTES = new PersonWithAttributes("foo","bar",42);
    public static final Page<PersonWithAttributes> PAGE_DOCUMENT = new Page<>(List.of(PERSON_WITH_ATTRIBUTES),null);
    public static final DocumentRef DOCUMENT_REF = new DocumentRef("123", new Module("Foo"));
    public static final Document DOCUMENT = new Document(
            "123",
            new Module("Foo"),
            Instant.parse("2023-12-15T01:01:01.0010010Z"),
            Map.of("first_name","foo", "last_name", "bar","age",42)
        );
    public static final NamedDocumentRef NAMED_DOCUMENT_REF = new NamedDocumentRef("Boogles", new Module("Foo"));
    public static final NamedDocument NAMED_DOCUMENT = new NamedDocument(
            "Boogles",
            new Module("Foo"),
            Instant.parse("2023-12-15T01:01:01.0010010Z"),
            Map.of("first_name","foo", "last_name", "bar")
    );
    public static final NullDocumentException NULL_DOC_EXCEPTION = new NullDocumentException("123", new Module("Foo"), "not found");

    // Codecs
    public static final Codec<Object> DYNAMIC_CODEC = DefaultCodecProvider.SINGLETON.get(Object.class);
    public static final Codec<String>  STRING_CODEC = DefaultCodecProvider.SINGLETON.get(String.class);
    public static final Codec<Character>  CHAR_CODEC = DefaultCodecProvider.SINGLETON.get(Character.class);
    public static final Codec<Boolean>  BOOL_CODEC = DefaultCodecProvider.SINGLETON.get(Boolean.class);
    public static final Codec<byte[]>  BYTE_ARRAY_CODEC = DefaultCodecProvider.SINGLETON.get(byte[].class);
    public static final Codec<Byte>  BYTE_CODEC = DefaultCodecProvider.SINGLETON.get(byte.class);
    public static final Codec<Short>  SHORT_CODEC = DefaultCodecProvider.SINGLETON.get(short.class);
    public static final Codec<Integer>  INT_CODEC = DefaultCodecProvider.SINGLETON.get(int.class);
    public static final Codec<Long>  LONG_CODEC = DefaultCodecProvider.SINGLETON.get(long.class);
    public static final Codec<Float>  FLOAT_CODEC = DefaultCodecProvider.SINGLETON.get(float.class);
    public static final Codec<Double>  DOUBLE_CODEC = DefaultCodecProvider.SINGLETON.get(double.class);
    public static final Codec<Optional<Integer>> OPTIONAL_INT_CODEC = (Codec<Optional<Integer>>) (Codec) DefaultCodecProvider.SINGLETON.get(Optional.class, int.class);
    public static final Codec<Optional<String>> OPTIONAL_STRING_CODEC = (Codec<Optional<String>>) (Codec) DefaultCodecProvider.SINGLETON.get(Optional.class, String.class);
    public static final Codec<Page<PersonWithAttributes>> PAGE_CODEC = (Codec<Page<PersonWithAttributes>>) (Codec) DefaultCodecProvider.SINGLETON.get(Page.class, PersonWithAttributes.class);
    public static final Codec<List<Integer>> LIST_INT_CODEC = (Codec<List<Integer>>) (Codec) DefaultCodecProvider.SINGLETON.get(List.class, int.class);
    public static final Codec<Map<String, Integer>> MAP_INT_CODEC = (Codec<Map<String,Integer>>) (Codec) DefaultCodecProvider.SINGLETON.get(Map.class, Integer.class);
    public static final Codec<Map<String, String>> MAP_STRING_CODEC = (Codec<Map<String,String>>) (Codec) DefaultCodecProvider.SINGLETON.get(Map.class, String.class);
    public static final Codec<ClassWithRefTagCollision> CLASS_WITH_REF_TAG_COLLISION_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithRefTagCollision.class);
    public static final Codec<ClassWithParameterizedFields>  CLASS_WITH_PARAMETERIZED_FIELDS_CODEC = DefaultCodecProvider.SINGLETON.get(ClassWithParameterizedFields.class);
    public static final Codec<PersonWithAttributes>  PERSON_WITH_ATTRIBUTES_CODEC = DefaultCodecProvider.SINGLETON.get(PersonWithAttributes.class);

}
