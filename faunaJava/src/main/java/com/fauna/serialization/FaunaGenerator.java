package com.fauna.serialization;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fauna.common.types.Module;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FaunaGenerator implements AutoCloseable {

    private final JsonGenerator jsonGenerator;

    /**
     * Initializes a new instance of the Utf8FaunaWriter class with a specified buffer writer.
     *
     * @param bufferWriter The buffer writer to write to.
     */
    public FaunaGenerator(ByteArrayBuilder bufferWriter) throws IOException {
        JsonFactory factory = new JsonFactory();
        this.jsonGenerator = factory.createGenerator(bufferWriter);
    }

    /**
     * Initializes a new instance of the Utf8FaunaWriter class with a specified stream.
     *
     * @param outputStream The stream to write to.
     */
    public FaunaGenerator(OutputStream outputStream) throws IOException {
        JsonFactory factory = new JsonFactory();
        this.jsonGenerator = factory.createGenerator(outputStream);
    }

    /**
     * Flushes the written data to the underlying buffer or stream.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void flush() throws IOException {
        jsonGenerator.flush();
    }

    /**
     * Writes the beginning of an object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeStartObject() throws IOException {
    }

    /**
     * Writes the end of an object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeEndObject() throws IOException {
    }

    /**
     * Writes the beginning of a specially tagged object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeStartEscapedObject() throws IOException {
    }

    /**
     * Writes the end of a specially tagged object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeEndEscapedObject() throws IOException {
    }

    /**
     * Writes the beginning of an array.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeStartArray() throws IOException {
    }

    /**
     * Writes the end of an array.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeEndArray() throws IOException {
    }

    /**
     * Writes the beginning of a reference object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeStartRef() throws IOException {
    }

    /**
     * Writes the end of a reference object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeEndRef() throws IOException {
    }

    /**
     * Writes a double value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The decimal value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeDouble(String fieldName, BigDecimal value) throws IOException {
    }

    /**
     * Writes a double value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The double value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeDouble(String fieldName, double value) throws IOException {
    }

    /**
     * Writes an integer value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The integer value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeInt(String fieldName, int value) throws IOException {
    }

    /**
     * Writes a long integer value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The long integer value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeLong(String fieldName, long value) throws IOException {
    }

    /**
     * Writes a string value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The string value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeString(String fieldName, String value) throws IOException {
    }

    /**
     * Writes a date value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The LocalDateTime value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeDate(String fieldName, LocalDateTime value) throws IOException {
    }

    /**
     * Writes a time value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The LocalDateTime value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTime(String fieldName, LocalDateTime value) throws IOException {
    }

    /**
     * Writes a boolean value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The boolean value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeBoolean(String fieldName, boolean value) throws IOException {
    }

    /**
     * Writes a null value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @throws IOException If an I/O error occurs.
     */
    public void writeNull(String fieldName) throws IOException {
    }

    /**
     * Writes a module value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The Module value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeModule(String fieldName, Module value) throws IOException {
    }

    /**
     * Writes a field name for the next value.
     *
     * @param value The name of the field.
     * @throws IOException If an I/O error occurs.
     */
    public void writeFieldName(String value) throws IOException {
    }

    /**
     * Writes a tagged value in an object.
     *
     * @param tag   The tag to use for the value.
     * @param value The value associated with the tag.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTaggedValue(String tag, String value) throws IOException {
    }

    @Override
    public void close() throws IOException {
        jsonGenerator.close();
    }
}
