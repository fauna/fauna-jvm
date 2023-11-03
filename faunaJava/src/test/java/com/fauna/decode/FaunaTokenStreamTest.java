package com.fauna.decode;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FaunaTokenStreamTest {

    private FaunaTokenStream faunaTokenStream;

    private void setupTokenStream(String json) throws IOException {
        faunaTokenStream = new FaunaTokenStream(new ByteArrayInputStream(json.getBytes()));
    }

    @Test
    public void testNextToken() throws IOException {
        setupTokenStream("{\"key\":\"value\"}");

        assertEquals(FaunaToken.FIELD_NAME, faunaTokenStream.nextToken());
        assertEquals(FaunaToken.VALUE_STRING, faunaTokenStream.nextToken());
        assertNull(faunaTokenStream.nextToken());
    }

    @Test
    void testFaunaIntToken() throws IOException {
        setupTokenStream("{\"@int\":\"123\"}");
        assertEquals(FaunaToken.VALUE_INT, faunaTokenStream.nextToken());
        assertEquals(123, faunaTokenStream.getValueAsInt());
    }

    @Test
    void testFaunaLongToken() throws IOException {
        setupTokenStream("{\"@long\":\"1234567890123\"}");

        assertEquals(FaunaToken.VALUE_LONG, faunaTokenStream.nextToken());
        assertEquals(1234567890123L, faunaTokenStream.getValueAsLong());
    }

    @Test
    void testFaunaDoubleToken() throws IOException {
        setupTokenStream("{\"@double\":\"123.456\"}");

        assertEquals(FaunaToken.VALUE_DOUBLE, faunaTokenStream.nextToken());
        assertEquals(123.456, faunaTokenStream.getValueAsDouble());
    }

    @Test
    void testFaunaTimeToken() throws IOException {
        setupTokenStream("{\"@time\":\"2023-11-20T13:33:10.300Z\"}");
        String dateTimeString = "2023-11-20T13:33:10.300Z";
        Instant instant = Instant.parse(dateTimeString);

        assertEquals(FaunaToken.VALUE_TIME, faunaTokenStream.nextToken());
        assertEquals(instant, faunaTokenStream.getValueAsTime());
    }

    @Test
    void testFaunaDateToken() throws IOException {
        setupTokenStream("{\"@date\":\"2023-11-20\"}");

        assertEquals(FaunaToken.VALUE_DATE, faunaTokenStream.nextToken());
        assertEquals(LocalDate.of(2023, 11, 20), faunaTokenStream.getValueAsDate());
    }

    @Test
    void testFaunaDocToken() throws IOException {
        setupTokenStream("{\"@doc\":{}}");

        assertEquals(FaunaToken.START_DOC, faunaTokenStream.nextToken());
        assertEquals(FaunaToken.END_DOC, faunaTokenStream.nextToken());
    }

    @Test
    void testFaunaModToken() throws IOException {
        setupTokenStream("{\"@mod\":\"moduleName\"}");

        assertEquals(FaunaToken.VALUE_MODULE, faunaTokenStream.nextToken());
        assertEquals("moduleName",faunaTokenStream.getValueAsString());
    }

    @Test
    void testFaunaRefToken() throws IOException {
        setupTokenStream("{\"@ref\":{\"id\":\"12345\"}}");

        assertEquals(FaunaToken.START_REF, faunaTokenStream.nextToken());

        assertEquals(FaunaToken.FIELD_NAME, faunaTokenStream.nextToken());
        assertEquals("id",faunaTokenStream.getValueAsString());

        assertEquals(FaunaToken.VALUE_STRING, faunaTokenStream.nextToken());
        assertEquals("12345",faunaTokenStream.getValueAsString());

        assertEquals(FaunaToken.END_REF, faunaTokenStream.nextToken());
        assertNull(faunaTokenStream.nextToken());
    }

    @Test
    void testCurrentTokenAccessors() throws IOException {
        setupTokenStream("{\"key\":123,\"boolean\":true}");

        assertEquals(FaunaToken.FIELD_NAME, faunaTokenStream.nextToken());
        assertEquals("key", faunaTokenStream.getValueAsString());

        assertEquals(FaunaToken.VALUE_INT, faunaTokenStream.nextToken());
        assertEquals(123, faunaTokenStream.getValueAsInt());

        assertEquals(FaunaToken.FIELD_NAME, faunaTokenStream.nextToken());
        assertEquals("boolean", faunaTokenStream.getValueAsString());

        assertEquals(FaunaToken.VALUE_TRUE, faunaTokenStream.nextToken());
        assertTrue(faunaTokenStream.getValueAsBoolean());

        assertNull(faunaTokenStream.nextToken());
    }


    @Test
    public void testStreamClosing() throws IOException {
        String json = "{}";
        faunaTokenStream = new FaunaTokenStream(new ByteArrayInputStream(json.getBytes()));

        assertDoesNotThrow(() -> faunaTokenStream.close());
    }
}