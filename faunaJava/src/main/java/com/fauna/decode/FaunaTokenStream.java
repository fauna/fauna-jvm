package com.fauna.decode;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * Implementation of the TokenStream interface for Fauna.
 * It provides a mechanism to read through JSON tokens from an InputStream,
 * typically the response body of an HTTP request to Fauna.
 */
public class FaunaTokenStream implements TokenStream {
    private static final String INT_TAG = "@int";
    private static final String LONG_TAG = "@long";
    private static final String DOUBLE_TAG = "@double";
    private static final String TIME_TAG = "@time";
    private static final String DATE_TAG = "@date";
    private static final String REF_TAG = "@ref";
    private static final String DOC_TAG = "@doc";
    private static final String MOD_TAG = "@mod";
    private static final String SET_TAG = "@set";

    private final Set<String> faunaTags = Set.of(
            INT_TAG, LONG_TAG, DOUBLE_TAG, TIME_TAG, DATE_TAG, REF_TAG, DOC_TAG, MOD_TAG, SET_TAG
    );
    private final JsonParser jsonParser;
    private boolean isFirstToken = true;

    /**
     * Constructs a FaunaTokenStream using the provided InputStream.
     *
     * @param body The InputStream containing JSON data to be parsed.
     * @throws IOException If an error occurs during the creation of the JsonParser.
     */
    public FaunaTokenStream(InputStream body) throws IOException {
        JsonFactory factory = new JsonFactory();
        this.jsonParser = factory.createParser(body);
        System.out.println("");
    }

    /**
     * Retrieves the next token in the stream, converting it to a FaunaToken.
     * The first call to this method returns the first token without advancing the stream.
     *
     * @return The next FaunaToken, or null if the end of the stream is reached.
     */
    @Override
    public FaunaToken nextToken() throws IOException {
        if (isFirstToken) {
            isFirstToken = false;
            jsonParser.nextToken(); // Advance to the first real token

            if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                jsonParser.nextToken(); // Move to field name or end object
                if (jsonParser.currentToken() == JsonToken.FIELD_NAME) {
                    return processPotentialFaunaTag();
                }
            }
        }

        JsonToken jsonToken = jsonParser.nextToken();
        if (jsonParser.currentToken() == JsonToken.END_OBJECT) {
            return determineEndFaunaToken();
        }
        return convertToFaunaToken(jsonToken);
    }

    /**
     * Determines the appropriate FaunaToken to represent the end of a JSON structure
     * based on the current field name. This method is called when the JsonParser
     * encounters an END_OBJECT token.
     *
     * @return The corresponding FaunaToken for the end of the JSON structure.
     * @throws IOException If there is an error reading from the JsonParser.
     */
    private FaunaToken determineEndFaunaToken() throws IOException {
        if (jsonParser.getCurrentName() == null) {
            return null;
        }
        switch (jsonParser.currentName()) {
            case REF_TAG:
                return FaunaToken.END_REF;
            case DOC_TAG:
                return FaunaToken.END_DOC;
            case SET_TAG:
                return FaunaToken.END_SET;
            default:
                return FaunaToken.END_OBJECT;
        }
    }

    /**
     * Processes a potential Fauna-specific tag when the current token is a FIELD_NAME.
     * If the field name matches a known Fauna tag, this method will convert it to the
     * corresponding FaunaToken. Otherwise, it returns FIELD_NAME.
     *
     * @return The FaunaToken corresponding to the field name or FIELD_NAME.
     * @throws IOException If there is an error reading from the JsonParser.
     */
    private FaunaToken processPotentialFaunaTag() throws IOException {
        String fieldName = jsonParser.getCurrentName();
        if (isFaunaTag(fieldName)) {
            jsonParser.nextToken(); // Move to the value token
            return convertToFaunaTagToken(fieldName);
        }
        return FaunaToken.FIELD_NAME;
    }

    /**
     * Checks if a given field name is a recognized Fauna tag.
     *
     * @param fieldName The field name to check.
     * @return true if the field name is a recognized Fauna tag; false otherwise.
     */
    private boolean isFaunaTag(String fieldName) {
        return faunaTags.contains(fieldName);
    }

    /**
     * Converts a recognized Fauna field name to the corresponding FaunaToken.
     * This method is used when a Fauna-specific tag is detected in the JSON input.
     *
     * @param fieldName The Fauna-specific field name.
     * @return The corresponding FaunaToken.
     * @throws IOException If there is an error reading from the JsonParser.
     */
    private FaunaToken convertToFaunaTagToken(String fieldName) throws IOException {
        // Convert field name to the corresponding FaunaToken
        switch (fieldName) {
            case INT_TAG:
                return FaunaToken.VALUE_INT;
            case LONG_TAG:
                return FaunaToken.VALUE_LONG;
            case DOUBLE_TAG:
                return FaunaToken.VALUE_DOUBLE;
            case TIME_TAG:
                return FaunaToken.VALUE_TIME;
            case DATE_TAG:
                return FaunaToken.VALUE_DATE;
            case REF_TAG:
                return FaunaToken.START_REF;
            case DOC_TAG:
                return FaunaToken.START_DOC;
            case MOD_TAG:
                return FaunaToken.VALUE_MODULE;
            case SET_TAG:
                return FaunaToken.START_SET;
            default:
                throw new IllegalStateException("Unknown Fauna tag: " + fieldName);
        }
    }

    /**
     * Converts a JsonToken to the corresponding FaunaToken.
     * Handles Fauna specific tokens like @int, @doc, etc.
     *
     * @param jsonToken The JsonToken to convert.
     * @return The corresponding FaunaToken.
     */
    private FaunaToken convertToFaunaToken(JsonToken jsonToken) {
        if (jsonToken == null) {
            return null;
        }
        switch (jsonToken) {
            case START_OBJECT:
                return FaunaToken.START_OBJECT;
            case END_OBJECT:
                return FaunaToken.END_OBJECT;
            case START_ARRAY:
                return FaunaToken.START_ARRAY;
            case END_ARRAY:
                return FaunaToken.END_ARRAY;
            case FIELD_NAME:
                return FaunaToken.FIELD_NAME;
            case VALUE_STRING:
                return FaunaToken.VALUE_STRING;
            case VALUE_NUMBER_INT:
                return FaunaToken.VALUE_INT;
            case VALUE_NUMBER_FLOAT:
                return FaunaToken.VALUE_DOUBLE;
            case VALUE_TRUE:
                return FaunaToken.VALUE_TRUE;
            case VALUE_FALSE:
                return FaunaToken.VALUE_FALSE;
            case VALUE_NULL:
                return FaunaToken.VALUE_NULL;
            default:
                throw new IllegalStateException("Unknown json token tag: " + jsonToken);
        }
    }

    /**
     * Closes the underlying JsonParser and releases any system resources associated with it.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        jsonParser.close();
    }

    @Override
    public String getValueAsString() {
        try {
            return jsonParser.getValueAsString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading current token as String", e);
        }
    }

    @Override
    public int getValueAsInt() {
        try {
            return jsonParser.getValueAsInt();
        } catch (IOException e) {
            throw new RuntimeException("Error reading current token as int", e);
        }
    }

    @Override
    public long getValueAsLong() {
        try {
            return jsonParser.getValueAsLong();
        } catch (IOException e) {
            throw new RuntimeException("Error reading current token as long", e);
        }
    }

    @Override
    public double getValueAsDouble() {
        try {
            return jsonParser.getValueAsDouble();
        } catch (IOException e) {
            throw new RuntimeException("Error reading current token as double", e);
        }
    }

    @Override
    public boolean getValueAsBoolean() {
        try {
            return jsonParser.getValueAsBoolean();
        } catch (IOException e) {
            throw new RuntimeException("Error reading current token as boolean", e);
        }
    }

    @Override
    public LocalDate getValueAsDate() {
        try {
            String valueAsString = jsonParser.getValueAsString();
            return LocalDate.parse(valueAsString);
        } catch (IOException e) {
            throw new RuntimeException("Error reading current token as LocalDate", e);
        }
    }

    @Override
    public Instant getValueAsTime() {
        try {
            String valueAsString = jsonParser.getValueAsString();
            return Instant.parse(valueAsString);
        } catch (IOException e) {
            throw new RuntimeException("Error reading current token as Instant", e);
        }
    }

}
