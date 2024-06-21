package com.fauna.serialization;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fauna.common.types.Module;
import com.fauna.exception.SerializationException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class FaunaGenerator implements AutoCloseable {

    private final JsonGenerator jsonGenerator;

    /**
     * Initializes a new instance of the FaunaGenerator class with a specified buffer writer.
     *
     * @param bufferWriter The buffer writer to write to.
     */
    public FaunaGenerator(ByteArrayBuilder bufferWriter) throws IOException {
        JsonFactory factory = new JsonFactory();
        this.jsonGenerator = factory.createGenerator(bufferWriter);
    }

    /**
     * Initializes a new instance of the FaunaGenerator class with a specified stream.
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
        jsonGenerator.writeStartObject();
    }

    /**
     * Writes the end of an object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeEndObject() throws IOException {
        jsonGenerator.writeEndObject();
    }

    /**
     * Writes the beginning of a specially tagged object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeStartEscapedObject() throws IOException {
        writeStartObject();
        writeFieldName("@object");
        writeStartObject();
    }

    /**
     * Writes the end of a specially tagged object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeEndEscapedObject() throws IOException {
        writeEndObject();
        writeEndObject();
    }

    /**
     * Writes the beginning of an array.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeStartArray() throws IOException {
        jsonGenerator.writeStartArray();
    }

    /**
     * Writes the end of an array.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeEndArray() throws IOException {
        jsonGenerator.writeEndArray();
    }

    /**
     * Writes the beginning of a reference object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeStartRef() throws IOException {
        writeStartObject();
        writeFieldName("@ref");
        writeStartObject();
    }

    /**
     * Writes the end of a reference object.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeEndRef() throws IOException {
        writeEndObject();
        writeEndObject();
    }

    /**
     * Writes a double value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The double value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeDouble(String fieldName, double value) throws IOException {
        writeFieldName(fieldName);
        writeDoubleValue(value);
    }

    /**
     * Writes an integer value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The integer value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeInt(String fieldName, int value) throws IOException {
        writeFieldName(fieldName);
        writeIntValue(value);
    }

    /**
     * Writes a long integer value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The long integer value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeLong(String fieldName, long value) throws IOException {
        writeFieldName(fieldName);
        writeLongValue(value);
    }

    /**
     * Writes a string value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The string value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeString(String fieldName, String value) throws IOException {
        writeFieldName(fieldName);
        writeStringValue(value);
    }

    /**
     * Writes a date value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The LocalDateTime value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeDate(String fieldName, LocalDate value) throws IOException {
        writeFieldName(fieldName);
        writeDateValue(value);
    }

    /**
     * Writes a time value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The LocalDateTime value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTime(String fieldName, Instant value) throws IOException {
        writeFieldName(fieldName);
        writeTimeValue(value);
    }

    /**
     * Writes a boolean value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The boolean value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeBoolean(String fieldName, boolean value) throws IOException {
        writeFieldName(fieldName);
        writeBooleanValue(value);
    }

    /**
     * Writes a null value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @throws IOException If an I/O error occurs.
     */
    public void writeNull(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeNullValue();
    }

    /**
     * Writes a module value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The Module value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeModule(String fieldName, Module value) throws IOException {
        writeFieldName(fieldName);
        writeModuleValue(value);
    }

    /**
     * Writes a field name for the next value.
     *
     * @param value The name of the field.
     * @throws IOException If an I/O error occurs.
     */
    public void writeFieldName(String value) throws IOException {
        jsonGenerator.writeFieldName(value);
    }

    /**
     * Writes a tagged value in an object.
     *
     * @param tag   The tag to use for the value.
     * @param value The value associated with the tag.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTaggedValue(String tag, String value) throws IOException {
        writeStartObject();
        writeString(tag, value);
        writeEndObject();
    }

    public void writeByteArray(byte[] bytes) throws IOException {
        writeTaggedValue("@bytes", Base64.getEncoder().encodeToString(bytes));
    }
    /**
     * Writes a double value as a tagged element.
     *
     * @param value The double value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeDoubleValue(double value) throws IOException {
        writeTaggedValue("@double", Double.toString(value));
    }

    /**
     * Writes an integer value as a tagged element.
     *
     * @param value The integer value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeIntValue(int value) throws IOException {
        writeTaggedValue("@int", Integer.toString(value));
    }

    /**
     * Writes a long integer value as a tagged element.
     *
     * @param value The long integer value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeLongValue(long value) throws IOException {
        writeTaggedValue("@long", Long.toString(value));
    }

    /**
     * Writes a string value as a tagged element.
     *
     * @param value The string value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeStringValue(String value) throws IOException {
        jsonGenerator.writeString(value);
    }

    /**
     * Writes a date value as a tagged element.
     *
     * @param value The date value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeDateValue(LocalDate value) throws IOException {
        try {
            String str = value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            writeTaggedValue("@date", str);
        } catch (DateTimeException e) {
            throw new SerializationException("Error writing date value", e);
        }
    }

    /**
     * Writes a time value as a tagged element.
     *
     * @param value The time value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeTimeValue(Instant value) throws IOException {
        try {
            Instant instant = value.atZone(ZoneOffset.UTC).toInstant();
            String formattedTime = instant.toString();
            writeTaggedValue("@time", formattedTime);
        } catch (DateTimeException e) {
            throw new SerializationException("Error writing time value", e);
        }
    }

    /**
     * Writes a boolean value to the stream.
     *
     * @param value The boolean value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeBooleanValue(boolean value) throws IOException {
        jsonGenerator.writeBoolean(value);
    }

    /**
     * Writes a null value to the stream.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeNullValue() throws IOException {
        jsonGenerator.writeNull();
    }

    /**
     * Writes a module value as a tagged element.
     *
     * @param value The module value to write.
     * @throws IOException If an I/O error occurs.
     */
    public void writeModuleValue(Module value) throws IOException {
        writeTaggedValue("@mod", value.getName());
    }

    @Override
    public void close() throws IOException {
        jsonGenerator.close();
    }
}
