package com.fauna.codec;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fauna.exception.CodecException;
import com.fauna.types.Module;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UTF8FaunaGenerator implements AutoCloseable {

    private final JsonGenerator jsonGenerator;
    private final ByteArrayOutputStream output;

    /**
     * Initializes a new instance of the FaunaGenerator class with a specified stream.
     *
     */
    public UTF8FaunaGenerator() throws IOException {

        JsonFactory factory = new JsonFactory();
        this.output = new ByteArrayOutputStream();
        this.jsonGenerator = factory.createGenerator(this.output);
    }

    public static UTF8FaunaGenerator create() throws CodecException {
        try {
            return new UTF8FaunaGenerator();
        } catch (IOException exc) {
            throw CodecException.encodingIOException(exc);
        }
    }

    /**
     * Flushes the written data to the underlying buffer or stream.
     *
     * @throws CodecException If an I/O error occurs.
     */
    public void flush() throws CodecException {
        try {
            jsonGenerator.flush();
        } catch (IOException exc) {
            throw CodecException.encodingIOException(exc);
        }
    }

    public String serialize() throws CodecException {
        this.flush();
        return this.output.toString(UTF_8);

    }



    /**
     * Writes the beginning of an object.
     *
     * @throws CodecException If an I/O error occurs.
     */
    public void writeStartObject() throws CodecException {
        try {
            jsonGenerator.writeStartObject();
        } catch (IOException exc) {
            throw CodecException.encodingIOException(exc);
        }
    }

    /**
     * Writes the end of an object.
     *
     * @throws CodecException If an I/O error occurs.
     */
    public void writeEndObject() throws CodecException {
        try {
            jsonGenerator.writeEndObject();
        } catch (IOException exc) {
            throw CodecException.encodingIOException(exc);
        }
    }

    /**
     * Writes the beginning of a specially tagged object.
     *
     * @throws CodecException If an I/O error occurs.
     */
    public void writeStartEscapedObject() throws CodecException {
        writeStartObject();
        writeFieldName("@object");
        writeStartObject();
    }

    /**
     * Writes the end of a specially tagged object.
     *
     * @throws CodecException If an I/O error occurs.
     */
    public void writeEndEscapedObject() throws CodecException {
        writeEndObject();
        writeEndObject();
    }

    /**
     * Writes the beginning of an array.
     *
     * @throws CodecException If an I/O error occurs.
     */
    public void writeStartArray() throws CodecException {
        try {
            jsonGenerator.writeStartArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the end of an array.
     *
     * @throws CodecException If an I/O error occurs.
     */
    public void writeEndArray() throws CodecException {
        try {
            jsonGenerator.writeEndArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the beginning of a reference object.
     *
     * @throws CodecException If an I/O error occurs.
     */
    public void writeStartRef() throws CodecException {
        writeStartObject();
        writeFieldName("@ref");
        writeStartObject();
    }

    /**
     * Writes the end of a reference object.
     *
     * @throws CodecException If an error occurs.
     */
    public void writeEndRef() throws CodecException {
        writeEndObject();
        writeEndObject();
    }

    /**
     * Writes a double value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The double value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeDouble(String fieldName, double value) throws CodecException {
        writeFieldName(fieldName);
        writeDoubleValue(value);
    }

    /**
     * Writes an integer value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The integer value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeInt(String fieldName, int value) throws CodecException {
        writeFieldName(fieldName);
        writeIntValue(value);
    }

    /**
     * Writes a long integer value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The long integer value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeLong(String fieldName, long value) throws CodecException {
        writeFieldName(fieldName);
        writeLongValue(value);
    }

    /**
     * Writes a string value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The string value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeString(String fieldName, String value) throws CodecException {
        writeFieldName(fieldName);
        writeStringValue(value);
    }

    /**
     * Writes a date value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The LocalDateTime value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeDate(String fieldName, LocalDate value) throws CodecException {
        writeFieldName(fieldName);
        writeDateValue(value);
    }

    /**
     * Writes a time value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The LocalDateTime value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeTime(String fieldName, Instant value) throws CodecException {
        writeFieldName(fieldName);
        writeTimeValue(value);
    }

    /**
     * Writes a boolean value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The boolean value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeBoolean(String fieldName, boolean value) throws CodecException {
        writeFieldName(fieldName);
        writeBooleanValue(value);
    }

    /**
     * Writes a null value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeNull(String fieldName) throws CodecException {
        writeFieldName(fieldName);
        writeNullValue();
    }

    /**
     * Writes a module value with a specific field name.
     *
     * @param fieldName The name of the field.
     * @param value     The Module value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeModule(String fieldName, Module value) throws CodecException {
        writeFieldName(fieldName);
        writeModuleValue(value);
    }

    /**
     * Writes a field name for the next value.
     *
     * @param value The name of the field.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeFieldName(String value) throws CodecException {
        try {
            jsonGenerator.writeFieldName(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a tagged value in an object.
     *
     * @param tag   The tag to use for the value.
     * @param value The value associated with the tag.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeTaggedValue(String tag, String value) throws CodecException {
        writeStartObject();
        writeString(tag, value);
        writeEndObject();
    }

    /**
     * Writes a double value as a tagged element.
     *
     * @param value The double value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeDoubleValue(double value) throws CodecException {
        writeTaggedValue("@double", Double.toString(value));
    }

    /**
     * Writes a float value as a tagged element (@double).
     *
     * @param value The float value to write as a double.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeDoubleValue(float value) throws CodecException {
        writeTaggedValue("@double", Float.toString(value));
    }


    /**
     * Writes an integer value as a tagged element.
     *
     * @param value The integer value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeIntValue(int value) throws CodecException {
        writeTaggedValue("@int", Integer.toString(value));
    }

    /**
     * Writes a long integer value as a tagged element.
     *
     * @param value The long integer value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeLongValue(long value) throws CodecException {
        writeTaggedValue("@long", Long.toString(value));
    }

    /**
     * Writes a string value as a tagged element.
     *
     * @param value The string value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeStringValue(String value) throws CodecException {
        try {
            jsonGenerator.writeString(value);
        } catch (IOException exc) {
            throw CodecException.encodingIOException(exc);
        }
    }

    /**
     * Writes a date value as a tagged element.
     *
     * @param value The date value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeDateValue(LocalDate value) throws CodecException {
        String str = value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        writeTaggedValue("@date", str);
    }

    /**
     * Writes a time value as a tagged element.
     *
     * @param value The time value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeTimeValue(Instant value) throws CodecException {
        Instant instant = value.atZone(ZoneOffset.UTC).toInstant();
        String formattedTime = instant.toString();
        writeTaggedValue("@time", formattedTime);
    }

    /**
     * Writes a boolean value to the stream.
     *
     * @param value The boolean value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeBooleanValue(boolean value) throws CodecException {
        try {
            jsonGenerator.writeBoolean(value);
        } catch (IOException exc) {
            throw CodecException.encodingIOException(exc);
        }
    }

    public void writeCharValue(Character value) throws CodecException {
        writeIntValue(value);
    }

    /**
     * Writes a null value to the stream.
     *
     * @throws CodecException If an I/O error occurs.
     */
    public void writeNullValue() throws CodecException {
        try {
            jsonGenerator.writeNull();
        } catch (IOException exc) {
            throw CodecException.encodingIOException(exc);
        }
    }

    /**
     * Writes a module value as a tagged element.
     *
     * @param value The module value to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeModuleValue(Module value) throws CodecException {
        writeTaggedValue("@mod", value.getName());
    }

    /**
     * Writes a byte array encoded as a base64 string as a tagged element.
     *
     * @param value The byte array to write.
     * @throws CodecException If an I/O error occurs.
     */
    public void writeBytesValue(byte[] value) throws CodecException {
        writeTaggedValue("@bytes", Base64.getEncoder().encodeToString(value));
    }

    @Override
    public void close() throws CodecException {
        try {
            jsonGenerator.close();
        } catch (IOException exc) {
            throw CodecException.encodingIOException(exc);
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                //noinspection ThrowFromFinallyBlock
                throw CodecException.encodingIOException(e);
            }
        }
    }
}
