package com.fauna.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fauna.beans.Person;
import com.fauna.beans.PersonWithAttributes;
import com.fauna.common.types.Module;
import com.fauna.mapping.MappingContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SerializerTest {

    public static String serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            FaunaGenerator faunaWriter = new FaunaGenerator(stream)) {
            MappingContext ctx = new MappingContext();
            Serializer.serialize(ctx, faunaWriter, obj);
            faunaWriter.flush();
            return new String(stream.toByteArray());
        }
    }

    @Test
    public void serializeValues() throws IOException {
        Instant dt = Instant.parse("2024-01-23T13:33:10.300Z");

        HashMap<String, Object> tests = new HashMap<>();
        tests.put("\"hello\"", "hello");
        tests.put("true", true);
        tests.put("false", false);
        tests.put("null", null);
        tests.put("{\"@date\":\"2023-12-13\"}", LocalDate.of(2023, 12, 13));
        tests.put("{\"@double\":\"1.2\"}", 1.2d);
        tests.put("{\"@double\":\"1.340000033378601\"}", 1.34f);
        tests.put("{\"@int\":\"1\"}", Byte.parseByte("1"));
        tests.put("{\"@int\":\"2\"}", Byte.parseByte("2"));
        tests.put("{\"@int\":\"40\"}", Short.parseShort("40"));
        tests.put("{\"@int\":\"41\"}", Short.parseShort("41"));
        tests.put("{\"@int\":\"42\"}", 42);
        tests.put("{\"@long\":\"43\"}", 43L);
        tests.put("{\"@mod\":\"module\"}", new Module("module"));
        tests.put("{\"@time\":\"2024-01-23T13:33:10.300Z\"}", dt);

        for (Map.Entry<String, Object> entry : tests.entrySet()) {
            String expected = entry.getKey();
            Object test = entry.getValue();
            String result = serialize(test);
            assertEquals(expected, result);
        }
    }

    @Test
    public void serializeDictionary() throws IOException {
        Map<String, Object> test = new HashMap<>();
        test.put("answer", 42);
        test.put("foo", "bar");
        test.put("list", new ArrayList<>());
        test.put("obj", new HashMap<>());

        String actual = serialize(test);
        assertEquals("{\"answer\":{\"@int\":\"42\"},\"obj\":{},\"foo\":\"bar\",\"list\":[]}",
            actual);
    }

    @Test
    public void serializeDictionaryWithTagConflicts() throws IOException {
        Map<Map<String, Object>, String> tests = new HashMap<>();
        tests.put(new HashMap<>() {{
            put("@date", "not");
        }}, "{\"@object\":{\"@date\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@doc", "not");
        }}, "{\"@object\":{\"@doc\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@double", "not");
        }}, "{\"@object\":{\"@double\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@int", "not");
        }}, "{\"@object\":{\"@int\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@long", "not");
        }}, "{\"@object\":{\"@long\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@mod", "not");
        }}, "{\"@object\":{\"@mod\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@object", "not");
        }}, "{\"@object\":{\"@object\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@ref", "not");
        }}, "{\"@object\":{\"@ref\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@set", "not");
        }}, "{\"@object\":{\"@set\":\"not\"}}");
        tests.put(new HashMap<>() {{
            put("@time", "not");
        }}, "{\"@object\":{\"@time\":\"not\"}}");

        for (Map.Entry<Map<String, Object>, String> entry : tests.entrySet()) {
            String expected = entry.getValue();
            Map<String, Object> test = entry.getKey();
            String actual = serialize(test);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void serializeList() throws IOException {
        List<Object> test = new ArrayList<>();
        test.add(42);
        test.add("foo bar");
        test.add(new ArrayList<>());
        test.add(new HashMap<>());

        String actual = serialize(test);
        assertEquals("[{\"@int\":\"42\"},\"foo bar\",[],{}]", actual);
    }

    @Test
    public void serializeClass() throws IOException {
        Person test = new Person("Baz", "Luhrmann", 61);
        String actual = serialize(test);
        assertEquals("{\"firstName\":\"Baz\",\"lastName\":\"Luhrmann\",\"age\":{\"@int\":\"61\"}}",
            actual);
    }

    @Test
    public void serializeClassWithAttributes() throws IOException {
        PersonWithAttributes test = new PersonWithAttributes("Baz", "Luhrmann", 61L);
        String actual = serialize(test);
        assertEquals(
            "{\"first_name\":\"Baz\",\"last_name\":\"Luhrmann\",\"age\":{\"@long\":\"61\"}}",
            actual);
    }

    /*
        @Test
        public void serializeClassWithTagConflicts() throws IOException {
            Map<Object, String> tests = new HashMap<>();
            tests.put(new PersonWithDateConflict(), "{\"@object\":{\"@date\":\"not\"}}");
            tests.put(new PersonWithDocConflict(), "{\"@object\":{\"@doc\":\"not\"}}");
            tests.put(new PersonWithDoubleConflict(), "{\"@object\":{\"@double\":\"not\"}}");
            tests.put(new PersonWithIntConflict(), "{\"@object\":{\"@int\":\"not\"}}");
            tests.put(new PersonWithLongConflict(), "{\"@object\":{\"@long\":\"not\"}}");
            tests.put(new PersonWithModConflict(), "{\"@object\":{\"@mod\":\"not\"}}");
            tests.put(new PersonWithObjectConflict(), "{\"@object\":{\"@object\":\"not\"}}");
            tests.put(new PersonWithRefConflict(), "{\"@object\":{\"@ref\":\"not\"}}");
            tests.put(new PersonWithSetConflict(), "{\"@object\":{\"@set\":\"not\"}}");
            tests.put(new PersonWithTimeConflict(), "{\"@object\":{\"@time\":\"not\"}}");

            for (Map.Entry<Object, String> entry : tests.entrySet()) {
                Object test = entry.getKey();
                String expected = entry.getValue();
                String actual = serialize(test);
                assertEquals(expected, actual);
            }
        }
        @Test
        public void serializeClassWithTypeConversions() throws IOException {
            PersonWithTypeOverrides test = new PersonWithTypeOverrides();
            String expectedWithWhitespace =
                "{\"short_to_long\":{\"@long\":\"10\"},\"ushort_to_long\":{\"@long\":\"11\"}," +
                    "\"byte_to_long\":{\"@long\":\"12\"},\"sbyte_to_long\":{\"@long\":\"13\"},\"int_to_long\":{\"@long\":\"20\"},"
                    +
                    "\"uint_to_long\":{\"@long\":\"21\"},\"long_to_long\":{\"@long\":\"30\"},\"short_to_int\":{\"@int\":\"40\"},"
                    +
                    "\"ushort_to_int\":{\"@int\":\"41\"},\"byte_to_int\":{\"@int\":\"42\"},\"sbyte_to_int\":{\"@int\":\"43\"},"
                    +
                    "\"int_to_int\":{\"@int\":\"50\"},\"short_to_double\":{\"@double\":\"60\"},\"int_to_double\":{\"@double\":\"70\"},"
                    +
                    "\"long_to_double\":{\"@double\":\"80\"},\"double_to_double\":{\"@double\":\"10.1\"},\"float_to_double\":{\"@double\":\"1.344499945640564\"},"
                    +
                    "\"true_to_true\":true,\"false_to_false\":false,\"class_to_string\":\"TheThing\",\"string_to_string\":\"aString\","
                    +
                    "\"datetime_to_date\":{\"@date\":\"2023-12-13\"},\"dateonly_to_date\":{\"@date\":\"2023-12-13\"},"
                    +
                    "\"datetimeoffset_to_date\":{\"@date\":\"2023-12-13\"},\"datetime_to_time\":{\"@time\":\"2023-12-13T12:12:12.0010010Z\"},"
                    +
                    "\"datetimeoffset_to_time\":{\"@time\":\"2023-12-13T12:12:12.0010010+00:00\"}}";
            String expected = expectedWithWhitespace.replaceAll("\\s", "");
            String actual = serialize(test);
            assertEquals(expected, actual);
        }

        @Test
        public void serializeObjectWithInvalidTypeHint() {
            ClassWithInvalidPropertyTypeHint obj = new ClassWithInvalidPropertyTypeHint();
            assertThrows(SerializationException.class, () -> serialize(obj));
        }

        @Test
        public void serializeObjectWithFieldAttributeAndWithoutObjectAttribute() throws IOException {
            ClassWithFieldAttributeAndWithoutObjectAttribute obj = new ClassWithFieldAttributeAndWithoutObjectAttribute();
            String expected = "{\"firstName\":\"Baz\"}";
            String actual = serialize(obj);
            assertEquals(expected, actual);
        }

        @Test
        public void serializeObjectWithPropertyWithoutFieldAttribute() throws IOException {
            ClassWithPropertyWithoutFieldAttribute obj = new ClassWithPropertyWithoutFieldAttribute();
            String expected = "{}";
            String actual = serialize(obj);
            assertEquals(expected, actual);
        }
    */
    @Test
    public void serializeAnonymousClassObject() throws IOException {
        Object obj = new Object() {
            public String firstName = "John";
            public String lastName = "Doe";
        };
        String expected = "{\"firstName\":\"John\",\"lastName\":\"Doe\"}";
        String actual = serialize(obj);
        assertEquals(expected, actual);
    }

    @Test
    public void serializeCollectionEdgeCases() throws IOException {
        Map<Object, String> tests = new HashMap<>();
        tests.put(new ArrayList<>(), "[]");
        tests.put(new HashMap<>(), "{}");
        tests.put(Arrays.asList(new ArrayList<>(), new HashMap<>()), "[[],{}]");

        for (Map.Entry<Object, String> entry : tests.entrySet()) {
            Object value = entry.getKey();
            String expected = entry.getValue();
            String actual = serialize(value);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void serializeExtremeNumericValues() throws IOException {
        Map<Object, String> tests = new HashMap<>();
        tests.put(Short.MAX_VALUE, "{\"@int\":\"32767\"}");
        tests.put(Short.MIN_VALUE, "{\"@int\":\"-32768\"}");
        tests.put(Integer.MAX_VALUE, "{\"@int\":\"2147483647\"}");
        tests.put(Integer.MIN_VALUE, "{\"@int\":\"-2147483648\"}");
        tests.put(Long.MAX_VALUE, "{\"@long\":\"9223372036854775807\"}");
        tests.put(Long.MIN_VALUE, "{\"@long\":\"-9223372036854775808\"}");
        tests.put(Double.MAX_VALUE, "{\"@double\":\"1.7976931348623157E308\"}");
        tests.put(Double.MIN_VALUE, "{\"@double\":\"4.9E-324\"}");

        for (Map.Entry<Object, String> entry : tests.entrySet()) {
            Object value = entry.getKey();
            String expected = entry.getValue();
            String actual = serialize(value);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void serializeNumericEdgeCases() throws IOException {
        Map<Object, String> tests = new HashMap<>();
        tests.put(Double.NaN, "{\"@double\":\"NaN\"}");
        tests.put(Double.POSITIVE_INFINITY, "{\"@double\":\"Infinity\"}");
        tests.put(Double.NEGATIVE_INFINITY, "{\"@double\":\"-Infinity\"}");

        for (Map.Entry<Object, String> entry : tests.entrySet()) {
            Object value = entry.getKey();
            String expected = entry.getValue();
            String actual = serialize(value);
            assertEquals(expected, actual);
        }
    }
}