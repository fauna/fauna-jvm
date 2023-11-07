package com.fauna.encoding;

import com.fauna.query.builder.Query;
import com.fauna.query.model.Document;
import com.fauna.query.model.DocumentReference;
import com.fauna.query.model.Module;
import com.fauna.query.model.NamedDocument;
import com.fauna.query.model.NamedDocumentReference;
import com.fauna.query.model.NullDocument;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FaunaEncoderTest {
    Map<String, Object> testMap;

    {
        testMap = new HashMap<>();
        testMap.put("int", 10);
        testMap.put("double", 10.0);
        testMap.put("long", 2147483649L);
        testMap.put("string", "foo");
        testMap.put("true", true);
        testMap.put("false", false);
        testMap.put("none", null);
        testMap.put("date", LocalDate.of(2023, 2, 28));
        testMap.put("time", LocalDateTime.of(2023, 2, 28, 10, 10, 10, 10000));

    }

    private Gson gson;

    @BeforeEach
    public void setUp() {
        gson = new GsonBuilder().serializeNulls().create();
    }


    @Test
    void testEncodeString() {
        String test = "hello";
        JsonElement expectedJson = JsonParser.parseString(test);

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeTrue() {
        Boolean test = true;
        JsonElement expectedJson = JsonParser.parseString(String.valueOf(test));

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeFalse() {
        Boolean test = false;
        JsonElement expectedJson = JsonParser.parseString(String.valueOf(test));

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeInt() {
        JsonElement expectedJson = JsonParser.parseString("{\"@int\":\"10\"}");
        int test = 10;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeMax32BitSignedInt() {
        JsonElement expectedJson = JsonParser.parseString("{\"@int\":\"2147483647\"}");
        int test = 2147483647;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeMin32BitSignedInt() {
        JsonElement expectedJson = JsonParser.parseString("{\"@int\":\"-2147483648\"}");
        int test = -2147483648;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeMax32BitSignedIntPlusOne() {
        JsonElement expectedJson = JsonParser.parseString("{\"@long\":\"2147483648\"}");
        long test = 2147483648L;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeMin32BitSignedIntMinusOne() {
        JsonElement expectedJson = JsonParser.parseString("{\"@long\":\"-2147483649\"}");
        long test = -2147483649L;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeMax64BitSignedInt() {
        JsonElement expectedJson = JsonParser.parseString("{\"@long\":\"9223372036854775807\"}");
        long test = 9223372036854775807L;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeMin64BitSignedInt() {
        JsonElement expectedJson = JsonParser.parseString("{\"@long\":\"-9223372036854775808\"}");
        long test = -9223372036854775808L;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);

    }


    @Test
    void testEncodeNegativeDouble() {
        JsonElement expectedJson = JsonParser.parseString("{\"@double\":\"-100.0\"}");
        double test = -100.0;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodePositiveDouble() {
        JsonElement expectedJson = JsonParser.parseString("{\"@double\":\"9.999999999999\"}");
        double test = 9.999999999999;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeNone() {
        JsonElement expectedJson = JsonParser.parseString("{\"foo\":null}");
        Map<String, Object> test = new HashMap<>();
        test.put("foo", null);

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeLocalDateTime() {
        JsonElement expectedJson = JsonParser.parseString("{\"@time\":\"2023-02-28T10:10:10.000001Z\"}");
        LocalDateTime testDateTime = LocalDateTime.of(2023, 2, 28, 10, 10, 10, 1000);

        String encoded = FaunaEncoder.encode(testDateTime);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeLocalDate() {
        JsonElement expectedJson = JsonParser.parseString("{\"@date\":\"2023-03-17\"}");
        LocalDate testDate = LocalDate.parse("2023-03-17");

        String encoded = FaunaEncoder.encode(testDate);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeDocumentReference() {
        JsonElement expectedJson = JsonParser.parseString("{'@ref': {'coll': {'@mod': 'Col'}, 'id': \"123\"}}");
        DocumentReference docRef = new DocumentReference(new Module("Col"), "123");

        String encoded = FaunaEncoder.encode(docRef);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeNullDocument() {
        JsonElement expectedJson = JsonParser.parseString("{\"@ref\": {\"id\": \"456\", \"coll\": {\"@mod\": \"NDCol\"}}}");
        NullDocument nullDoc = new NullDocument(new DocumentReference(new Module("NDCol"), "456"), "not found");

        String encoded = FaunaEncoder.encode(nullDoc);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeNamedNullDocument() {
        JsonElement expectedJson = JsonParser.parseString("{\"@ref\": {\"name\": \"Party\", \"coll\": {\"@mod\": \"Collection\"}}}");
        NullDocument nullDoc = new NullDocument(new NamedDocumentReference("Collection", "Party"), "not found");

        String encoded = FaunaEncoder.encode(nullDoc);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeNamedDocumentReference() {
        JsonElement expectedJson = JsonParser.parseString("{\"@ref\": {\"name\": \"Hi\", \"coll\": {\"@mod\": \"Col\"}}}");
        NamedDocumentReference docRef = new NamedDocumentReference("Col", "Hi");

        String encoded = FaunaEncoder.encode(docRef);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeDocument() {
        JsonElement expectedJson = JsonParser.parseString("{\"@ref\": {\"id\": \"123\", \"coll\": {\"@mod\": \"Dogs\"}}}");

        LocalDateTime fixedDatetime = LocalDateTime.now();
        Module dogsModule = new Module("Dogs");
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Scout");
        Document testDoc = new Document("123", fixedDatetime, dogsModule, data);

        String encoded = FaunaEncoder.encode(testDoc);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeNamedDocuments() {
        JsonElement expectedJson = JsonParser.parseString("{\"@ref\": {\"name\": \"DogSchema\", \"coll\": {\"@mod\": \"Dogs\"}}}");
        LocalDateTime fixedDatetime = LocalDateTime.now();
        NamedDocument testNamedDoc = new NamedDocument("DogSchema", fixedDatetime, new Module("Dogs"), new HashMap<>());

        String encoded = FaunaEncoder.encode(testNamedDoc);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeModule() {
        JsonElement expectedJson = JsonParser.parseString("{\"@mod\": \"Math\"}");
        Module testModule = new Module("Math");

        String encoded = FaunaEncoder.encode(testModule);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeList() {
        JsonElement expectedJson = JsonParser.parseString("[{\"@date\":\"2023-02-28\"}, \"foo\", {\"@double\":\"10.0\"}, true, false, null, {\"@time\":\"2023-02-28T10:10:10.000010Z\"}, {\"@int\":\"10\"}, {\"@long\":\"2147483649\"}]");
        Object[] testValues = testMap.values().toArray();

        String encoded = FaunaEncoder.encode(Arrays.asList(testValues));
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeMap() {
        JsonElement expectedJson = JsonParser.parseString("{\"date\":{\"@date\":\"2023-02-28\"},\"string\":\"foo\",\"double\":{\"@double\":\"10.0\"},\"true\":true,\"false\":false,\"none\":null,\"time\":{\"@time\":\"2023-02-28T10:10:10.000010Z\"},\"int\":{\"@int\":\"10\"},\"long\":{\"@long\":\"2147483649\"}}");

        String encoded = FaunaEncoder.encode(testMap);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeIntConflictWithIntType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@int\": {\"@int\": \"10\"}}}");

        Map<String, Object> test = Map.of("@int", 10);

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeIntConflictWithOtherType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@int\": \"bar\"}}");

        Map<String, Object> test = Map.of("@int", "bar");

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);

    }

    @Test
    void testEncodeLongConflictWithLongType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@long\": {\"@long\": \"2147483649\"}}}");

        Map<String, Object> test = Map.of("@long", 2147483649L);

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeLongConflictWithOtherType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@long\": \"bar\"}}");

        Map<String, Object> test = Map.of("@long", "bar");

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeDoubleConflictWithDoubleType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@double\": {\"@double\": \"10.2\"}}}");

        Map<String, Object> test = Map.of("@double", 10.2);

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);

    }

    @Test
    public void testEncodeDoubleConflictWithOtherType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@double\": \"bar\"}}");

        Map<String, Object> test = Map.of("@double", "bar");

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);

    }

    @Test
    public void testEncodeDateConflictWithDateType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@date\": {\"@date\": \"2023-02-28\"}}}");

        Map<String, Object> test = Map.of("@date", LocalDate.of(2023, 2, 28));

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeDateConflictWithOtherType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@date\": \"bar\"}}");

        Map<String, Object> test = Map.of("@date", "bar");

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeTimeConflictWithDateTimeType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@time\": {\"@time\": \"2023-02-28T10:10:10.000001Z\"}}}");
        LocalDateTime dateTime = LocalDateTime.of(2023, 2, 28, 10, 10, 10, 1000);

        Map<String, Object> test = Map.of("@time", dateTime);

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeTimeConflictWithOtherType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@time\": \"bar\"}}");

        Map<String, Object> test = Map.of("@time", "bar");

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeRefConflictWithRefType() {
        JsonElement expectedJson = JsonParser.parseString("{\n" +
                "        \"@object\": {\n" +
                "            \"@ref\": {\n" +
                "                \"@ref\": {\n" +
                "                    \"id\": \"123\",\n" +
                "                    \"coll\": {\n" +
                "                        \"@mod\": \"Col\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }");

        Map<String, Object> test = Map.of("@ref", DocumentReference.fromString("Col:123"));
        ;

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeDocConflictWithOtherType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@doc\": \"bar\"}}");

        Map<String, Object> test = Map.of("@doc", "bar");

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeModConflictWithModType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@mod\": {\"@mod\": \"Math\"}}}");

        Map<String, Object> test = Map.of("@mod", new Module("Math"));

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeModConflictWithOtherType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@mod\": \"bar\"}}");

        Map<String, Object> test = Map.of("@mod", "bar");

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeObjectConflictsWithType() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@object\": {\"@int\": \"10\"}}}");

        Map<String, Object> test = Map.of("@object", 10);

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeObjectConflictsWithInt() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@object\": {\"@object\": {\"@int\": \"bar\"}}}}");

        Map<String, Map<String, String>> test = Map.of("@object", Map.of("@int", "bar"));

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeObjectConflictsWithObject() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@object\": {\"@object\": {\"@object\": \"bar\"}}}}");

        Map<String, Map<String, String>> test = Map.of("@object", Map.of("@object", "bar"));

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeMultipleKeysInConflict_NonConflicting() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@int\": \"foo\", \"tree\": \"birch\"}}");

        Map<String, String> test = Map.of(
                "@int", "foo",
                "tree", "birch"
        );

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeMultipleKeysInConflict_Conflicting() {
        JsonElement expectedJson = JsonParser.parseString("{\"@object\": {\"@int\": \"foo\", \"@double\": \"birch\"}}");

        Map<String, Object> test = Map.of(
                "@int", "foo",
                "@double", "birch"
        );

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeNestedConflict() {
        JsonElement expectedJson = JsonParser.parseString("{\n" +
                "        \"@object\": {\n" +
                "            \"@int\": {\n" +
                "                \"@object\": {\n" +
                "                    \"@date\": {\n" +
                "                        \"@object\": {\n" +
                "                            \"@time\": {\n" +
                "                                \"@object\": {\n" +
                "                                    \"@long\": {\n" +
                "                                        \"@int\": \"10\"\n" +
                "                                    }\n" +
                "                                }\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }");

        // Construct nested map structure with nested reserved keywords
        Map<String, Object> innermostMap = new HashMap<>();
        innermostMap.put("@long", 10); // This should be wrapped as "@long": "@int": "10"
        Map<String, Object> timeMap = new HashMap<>();
        timeMap.put("@time", innermostMap); // This should be wrapped into another @object due to @time conflict
        Map<String, Object> dateMap = new HashMap<>();
        dateMap.put("@date", timeMap); // Again, wrapped due to @date conflict
        Map<String, Object> intMap = new HashMap<>();
        intMap.put("@int", dateMap); // Top-level conflict, wrap this one too

        String encoded = FaunaEncoder.encode(intMap);
        JsonElement actualJson = JsonParser.parseString(encoded);


        assertEquals(expectedJson, actualJson);
    }

    @Test
    void testEncodeNonConflictingAtPrefix() {
        JsonElement expectedJson = JsonParser.parseString("{\"@foo\": {\"@int\": \"10\"}}");
        Map<String, Object> test = new HashMap<>();
        test.put("@foo", 10);

        String encoded = FaunaEncoder.encode(test);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeComplexObject() {
        JsonElement expectedJson = JsonParser.parseString("{\n" +
                "  \"bugs_coll\": {\n" +
                "    \"@mod\": \"Bugs\"\n" +
                "  },\n" +
                "  \"bug\": {\n" +
                "    \"@ref\": {\n" +
                "      \"id\": \"123\",\n" +
                "      \"coll\": {\n" +
                "        \"@mod\": \"Bugs\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"name\": \"fir\",\n" +
                "  \"age\": {\n" +
                "    \"@int\": \"200\"\n" +
                "  },\n" +
                "  \"birthdate\": {\n" +
                "    \"@date\": \"1823-02-08\"\n" +
                "  },\n" +
                "  \"molecules\": {\n" +
                "    \"@long\": \"999999999999999999\"\n" +
                "  },\n" +
                "  \"circumference\": {\n" +
                "    \"@double\": \"3.82\"\n" +
                "  },\n" +
                "  \"created_at\": {\n" +
                "    \"@time\": \"2003-02-08T13:28:12.555000Z\"\n" +
                "  },\n" +
                "  \"extras\": {\n" +
                "    \"nest\": {\n" +
                "      \"@object\": {\n" +
                "        \"@object\": {\n" +
                "          \"egg\": {\n" +
                "            \"fertilized\": false\n" +
                "          }\n" +
                "        },\n" +
                "        \"num_sticks\": {\n" +
                "          \"@int\": \"58\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"measurements\": [\n" +
                "    {\n" +
                "      \"id\": {\n" +
                "        \"@int\": \"1\"\n" +
                "      },\n" +
                "      \"employee\": {\n" +
                "        \"@int\": \"3\"\n" +
                "      },\n" +
                "      \"time\": {\n" +
                "        \"@time\": \"2013-02-08T12:00:05.123000Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": {\n" +
                "        \"@int\": \"2\"\n" +
                "      },\n" +
                "      \"employee\": {\n" +
                "        \"@int\": \"5\"\n" +
                "      },\n" +
                "      \"time\": {\n" +
                "        \"@time\": \"2023-02-08T14:22:01.000001Z\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // Construct the complex object structure
        Map<String, Object> complexObject = new HashMap<>();
        complexObject.put("bugs_coll", new Module("Bugs"));
        complexObject.put("bug", DocumentReference.fromString("Bugs:123"));
        complexObject.put("name", "fir");
        complexObject.put("age", 200);
        complexObject.put("birthdate", LocalDate.of(1823, 2, 8));
        complexObject.put("molecules", 999999999999999999L);
        complexObject.put("circumference", 3.82);
        complexObject.put("created_at", LocalDateTime.of(2003, 2, 8, 13, 28, 12, 555000000));

        Map<String, Object> nestMap = new HashMap<>();
        nestMap.put("num_sticks", 58);
        nestMap.put("@object", new HashMap<String, Object>() {{
            put("egg", new HashMap<String, Object>() {{
                put("fertilized", false);
            }});
        }});

        complexObject.put("extras", new HashMap<String, Object>() {{
            put("nest", nestMap);
        }});

        List<Map<String, Object>> measurements = Arrays.asList(
                new HashMap<>() {{
                    put("id", 1);
                    put("employee", 3);
                    put("time", LocalDateTime.of(2013, 2, 8, 12, 0, 5, 123000000));
                }},
                new HashMap<>() {{
                    put("id", 2);
                    put("employee", 5);
                    put("time", LocalDateTime.of(2023, 2, 8, 14, 22, 1, 1000));
                }}
        );
        complexObject.put("measurements", measurements);

        String encoded = FaunaEncoder.encode(complexObject);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);

    }

    @Test
    public void testEncodeLargeList() {
        List<Integer> testList = Collections.nCopies(10000, 10);

        JsonArray expectedJsonArray = new JsonArray();
        testList.forEach(value -> {
            JsonObject intObject = new JsonObject();
            intObject.addProperty("@int", value.toString());
            expectedJsonArray.add(intObject);
        });

        String encoded = FaunaEncoder.encode(testList);
        JsonArray parsedEncodedArray = new Gson().fromJson(encoded, JsonArray.class);

        assertEquals(expectedJsonArray, parsedEncodedArray);
    }

    @Test
    public void testEncodeLargeDict() {
        Map<String, Integer> testMap = new HashMap<>();
        IntStream.rangeClosed(1, 9999).forEach(i -> testMap.put("k" + i, i));

        // Expected JSON structure
        JsonObject expectedJsonObject = new JsonObject();
        testMap.forEach((key, value) -> {
            JsonObject intObject = new JsonObject();
            intObject.addProperty("@int", value.toString());
            expectedJsonObject.add(key, intObject);
        });

        String encoded = FaunaEncoder.encode(testMap);
        JsonObject parsedEncodedObject = new Gson().fromJson(encoded, JsonObject.class);

        assertEquals(expectedJsonObject, parsedEncodedObject);
    }

    @Test
    public void testEncodeDeepNestingInDict() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("k1", "v");

        Map<String, Object> currentMap = testMap;
        for (int i = 2; i < 300; i++) {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("k" + i, "v");
            currentMap.put("k" + i, newMap);
            currentMap = newMap; // Move the pointer to the new nested map
        }

        String encoded = FaunaEncoder.encode(testMap);

        JsonElement parsedEncoded = gson.fromJson(encoded, JsonElement.class);

        // Recursively build the expected JSON structure
        JsonObject expectedOuterObject = new JsonObject();
        JsonObject currentObject = expectedOuterObject;
        for (int i = 2; i < 300; i++) {
            JsonObject newObject = new JsonObject();
            newObject.addProperty("k" + i, "v");
            currentObject.add("k" + i, newObject);
            currentObject = newObject; // Move the pointer to the new nested object
        }
        expectedOuterObject.addProperty("k1", "v"); // Add the initial pair

        assertEquals(expectedOuterObject, parsedEncoded);
    }

    @Test
    public void testEncodePureStringQuery() {
        JsonElement expectedJson = JsonParser.parseString("{\"fql\": [\"let x = 11\"]}");
        Query query = fql("let x = 11", Map.of());

        String encoded = FaunaEncoder.encode(query);
        JsonElement actualJson = JsonParser.parseString(encoded);

        Assertions.assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodePureStringQueryWithBraces() {
        JsonElement expectedJson = JsonParser.parseString("{\"fql\": [\"let x = { y: 11 }\"]}");
        Query query = fql("let x = { y: 11 }", Map.of());

        String encoded = FaunaEncoder.encode(query);
        JsonElement actualJson = JsonParser.parseString(encoded);

        Assertions.assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeQueryBuilderWithFaunaStringInterpolation() {
        JsonElement expectedJson = JsonParser.parseString("{\n" +
                "      \"fql\": [\n" +
                "          \"let age = \",\n" +
                "          {\n" +
                "              \"value\": {\n" +
                "                  \"@int\": \"5\"\n" +
                "              }\n" +
                "          },\n" +
                "          \"\\n\\\"Alice is #{age} years old.\\\"\"\n" +
                "      ]\n" +
                "  }");

        Map<String, Object> args = new HashMap<>();
        args.put("n1", 5);
        Query query = Query.fql("let age = ${n1}\n\"Alice is #{age} years old.\"", args);

        String encoded = FaunaEncoder.encode(query);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeQueryBuilderWithValue() {
        JsonElement expectedJson = JsonParser.parseString("{\n" +
                "        \"fql\": [\n" +
                "            \"let x = \", {\n" +
                "                'value': {\n" +
                "                    'name': 'Dino',\n" +
                "                    'age': {\n" +
                "                        '@int': '0'\n" +
                "                    },\n" +
                "                    'birthdate': {\n" +
                "                        '@date': '2023-02-24'\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }");

        Map<String, Object> user = new HashMap<>();
        user.put("name", "Dino");
        user.put("age", 0);
        user.put("birthdate", LocalDate.of(2023, 2, 24));
        Map<String, Object> args = new HashMap<>();
        args.put("my_var", user);
        Query query = Query.fql("let x = ${my_var}", args);

        String encoded = FaunaEncoder.encode(query);
        JsonElement actualJson = JsonParser.parseString(encoded);


        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testEncodeQueryBuilderSubQueries() {
        JsonElement expectedJson = JsonParser.parseString("{\n" +
                "        \"fql\": [{\n" +
                "            \"fql\": [\n" +
                "                \"let x = \", {\n" +
                "                    'value': {\n" +
                "                        'name': 'Dino',\n" +
                "                        'age': {\n" +
                "                            '@int': '0'\n" +
                "                        },\n" +
                "                        'birthdate': {\n" +
                "                            '@date': '2023-02-24'\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            ]\n" +
                "        }, \"\\nx { .name }\"]\n" +
                "    }");

        Map<String, Object> user = new HashMap<>();
        user.put("name", "Dino");
        user.put("age", 0);
        user.put("birthdate", LocalDate.of(2023, 2, 24));
        Map<String, Object> innerArgs = new HashMap<>();
        innerArgs.put("my_var", user);
        Query innerQuery = Query.fql("let x = ${my_var}", innerArgs);

        Map<String, Object> outerArgs = new HashMap<>();
        outerArgs.put("inner", innerQuery);
        Query outerQuery = Query.fql("${inner}\nx { .name }", outerArgs);

        String encoded = FaunaEncoder.encode(outerQuery);
        JsonElement actualJson = JsonParser.parseString(encoded);

        assertEquals(expectedJson, actualJson);
    }

}