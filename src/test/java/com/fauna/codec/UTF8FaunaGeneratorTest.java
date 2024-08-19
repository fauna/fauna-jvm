package com.fauna.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fauna.types.Module;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UTF8FaunaGeneratorTest {

    private UTF8FaunaGenerator writer;

    @BeforeEach
    public void setUp() throws IOException {
        writer = new UTF8FaunaGenerator();
    }

    @AfterEach
    public void tearDown() throws IOException {
        writer.close();
    }

    @Test
    public void writeIntValue() throws IOException {
        writer.writeIntValue(42);
        assertWriter("{\"@int\":\"42\"}");
    }

    @Test
    public void writeLongValue() throws IOException {
        writer.writeLongValue(42L);
        assertWriter("{\"@long\":\"42\"}");
    }

    @Test
    public void writeDoubleValue() throws IOException {
        writer.writeDoubleValue(1.2d);
        assertWriter("{\"@double\":\"1.2\"}");
    }

    @Test
    public void writeTrueValue() throws IOException {
        writer.writeBooleanValue(true);
        assertWriter("true");
    }

    @Test
    public void writeFalseValue() throws IOException {
        writer.writeBooleanValue(false);
        assertWriter("false");
    }

    @Test
    public void writeNullValue() throws IOException {
        writer.writeNullValue();
        assertWriter("null");
    }

    @Test
    public void writeDate() throws IOException {
        LocalDate dateTime = LocalDate.of(2023, 1, 1);
        writer.writeDateValue(dateTime);
        assertWriter("{\"@date\":\"2023-01-01\"}");
    }

    @Test
    public void writeTime() throws IOException {
        Instant instant = Instant.parse("2024-01-23T13:33:10.300Z");
        writer.writeTimeValue(instant);
        assertWriter("{\"@time\":\"2024-01-23T13:33:10.300Z\"}");
    }

    @Test

    public void writeObject() throws IOException {
        writer.writeStartObject();
        writer.writeInt("anInt", 42);
        writer.writeLong("aLong", 42L);
        writer.writeDouble("aDouble", 1.2d);
        writer.writeDouble("aDecimal", 3.14);
        writer.writeBoolean("true", true);
        writer.writeBoolean("false", false);
        writer.writeString("foo", "bar");
        writer.writeDate("aDate", LocalDate.of(2023, 12, 4));
        writer.writeTime("aTime", Instant.parse("2024-01-23T13:33:10.300Z"));
        writer.writeNull("aNull");
        writer.writeFieldName("anArray");
        writer.writeStartArray();
        writer.writeEndArray();
        writer.writeFieldName("anObject");
        writer.writeStartObject();
        writer.writeEndObject();
        writer.writeEndObject();

        assertWriter("{\"anInt\":{\"@int\":\"42\"}," +
            "\"aLong\":{\"@long\":\"42\"}," +
            "\"aDouble\":{\"@double\":\"1.2\"}," +
            "\"aDecimal\":{\"@double\":\"3.14\"}," +
            "\"true\":true," +
            "\"false\":false," +
            "\"foo\":\"bar\"," +
            "\"aDate\":{\"@date\":\"2023-12-04\"}," +
            "\"aTime\":{\"@time\":\"2024-01-23T13:33:10.300Z\"}," +
            "\"aNull\":null," +
            "\"anArray\":[]," +
            "\"anObject\":{}}");
    }

    @Test
    public void writeArray() throws IOException {
        writer.writeStartArray();
        writer.writeIntValue(42);
        writer.writeLongValue(42L);
        writer.writeDoubleValue(1.2d);
        writer.writeDoubleValue(3.14);
        writer.writeBooleanValue(true);
        writer.writeBooleanValue(false);
        writer.writeStringValue("bar");
        writer.writeDateValue(LocalDate.of(2023, 12, 4));
        writer.writeTimeValue(Instant.parse("2024-01-23T13:33:10.300Z"));
        writer.writeNullValue();
        writer.writeStartArray();
        writer.writeEndArray();
        writer.writeStartObject();
        writer.writeEndObject();
        writer.writeEndArray();

        assertWriter("[{\"@int\":\"42\"}," +
            "{\"@long\":\"42\"}," +
            "{\"@double\":\"1.2\"}," +
            "{\"@double\":\"3.14\"}," +
            "true," +
            "false," +
            "\"bar\"," +
            "{\"@date\":\"2023-12-04\"}," +
            "{\"@time\":\"2024-01-23T13:33:10.300Z\"}," +
            "null," +
            "[]," +
            "{}]");
    }

    @Test
    public void writeEscapedObject() throws IOException {
        writer.writeStartEscapedObject();
        writer.writeEndEscapedObject();

        assertWriter("{\"@object\":{}}");
    }

    @Test
    public void writeRef() throws IOException {
        writer.writeStartRef();
        writer.writeString("id", "123");
        writer.writeModule("coll", new Module("Authors"));
        writer.writeEndRef();

        assertWriter("{\"@ref\":{\"id\":\"123\",\"coll\":{\"@mod\":\"Authors\"}}}");
    }

    public void writeTimeWithSixDecimalPrecision() throws IOException {
        Instant instant = Instant.parse("2024-01-23T13:33:10.300001Z");
        writer.writeTimeValue(instant);
        assertWriter("{\"@time\":\"2024-01-23T13:33:10.300001Z\"}");
    }

    @Test
    public void writeNonUTCTime() throws IOException {
        Instant instant = Instant.parse("2024-01-23T13:33:10.300001-07:00");
        writer.writeTimeValue(instant);
        assertWriter("{\"@time\":\"2024-01-23T20:33:10.300001Z\"}");
    }

    private void assertWriter(String expected) throws IOException {
        writer.flush();
        String actual = writer.serialize();
        assertEquals(expected, actual);
    }
}