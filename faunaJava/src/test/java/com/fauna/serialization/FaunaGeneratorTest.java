package com.fauna.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FaunaGeneratorTest {

    private FaunaGenerator writer;
    private ByteArrayOutputStream stream;

    @BeforeEach
    public void setUp() throws IOException {
        stream = new ByteArrayOutputStream();
        writer = new FaunaGenerator(stream);
    }

    @AfterEach
    public void tearDown() throws IOException {
        writer.close();
        stream.close();
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

    private void assertWriter(String expected) throws IOException {
        writer.flush();
        String actual = new String(stream.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(expected, actual);
    }
}