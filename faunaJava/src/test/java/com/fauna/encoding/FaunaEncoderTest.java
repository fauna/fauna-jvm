package com.fauna.encoding;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FaunaEncoderTest {

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

}