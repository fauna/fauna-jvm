package com.fauna.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.exception.CodecException;
import com.fauna.types.Module;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

/**
 * Represents a reader that provides fast, non-cached, forward-only access to serialized data.
 */
public final class UTF8FaunaParser {

    private static final String INT_TAG = "@int";
    private static final String LONG_TAG = "@long";
    private static final String DOUBLE_TAG = "@double";
    private static final String TIME_TAG = "@time";
    private static final String DATE_TAG = "@date";
    private static final String REF_TAG = "@ref";
    private static final String DOC_TAG = "@doc";
    private static final String MOD_TAG = "@mod";
    private static final String SET_TAG = "@set";
    private static final String STREAM_TAG = "@stream";
    private static final String OBJECT_TAG = "@object";
    private static final String BYTES_TAG = "@bytes";

    private final JsonParser jsonParser;
    private final Stack<Object> tokenStack = new Stack<>();
    private final Set<FaunaTokenType> closers = new HashSet<>(Arrays.asList(
            FaunaTokenType.END_OBJECT,
            FaunaTokenType.END_PAGE,
            FaunaTokenType.END_DOCUMENT,
            FaunaTokenType.END_REF,
            FaunaTokenType.END_ARRAY
    ));

    private FaunaTokenType currentFaunaTokenType = FaunaTokenType.NONE;
    private FaunaTokenType bufferedFaunaTokenType;
    private Object bufferedTokenValue;
    private String taggedTokenValue;

    private enum InternalTokenType {
        START_ESCAPED_OBJECT,
        START_PAGE_UNMATERIALIZED
    }

    /**
     * Constructs a {@code UTF8FaunaParser} instance with the given JSON parser.
     *
     * @param jsonParser The {@link JsonParser} used to read the JSON data.
     */
    public UTF8FaunaParser(final JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    /**
     * Creates a {@code UTF8FaunaParser} from an {@link InputStream}.
     *
     * @param body The input stream of JSON data.
     * @return A {@code UTF8FaunaParser} instance.
     * @throws CodecException if an {@link IOException} occurs while creating the parser.
     */
    public static UTF8FaunaParser fromInputStream(final InputStream body)
            throws CodecException {
        JsonFactory factory = new JsonFactory();
        try {
            JsonParser jsonParser = factory.createParser(body);
            UTF8FaunaParser faunaParser = new UTF8FaunaParser(jsonParser);
            if (faunaParser.getCurrentTokenType() == FaunaTokenType.NONE) {
                faunaParser.read();
            }
            return faunaParser;
        } catch (IOException exc) {
            throw CodecException.decodingIOException(exc);
        }
    }

    /**
     * Creates a {@code UTF8FaunaParser} from a JSON string
     *
     * @param str The JSON string.
     * @return A {@code UTF8FaunaParser} instance.
     */
    public static UTF8FaunaParser fromString(String str) {
        return UTF8FaunaParser.fromInputStream(
                new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Retrieves the current Fauna token type.
     *
     * @return The {@link FaunaTokenType} currently being processed.
     */
    public FaunaTokenType getCurrentTokenType() {
        return currentFaunaTokenType;
    }

    /**
     * Skips the current object or array in the JSON data.
     */
    public void skip() {
        switch (getCurrentTokenType()) {
            case START_OBJECT:
            case START_ARRAY:
            case START_PAGE:
            case START_REF:
            case START_DOCUMENT:
                skipInternal();
                break;
        }
    }

    private void skipInternal() {
        int startCount = tokenStack.size();
        while (read()) {
            if (tokenStack.size() < startCount) {
                break;
            }
        }
    }

    /**
     * Reads the next token from the JSON parser.
     *
     * @return {@code true} if there is another token to read, {@code false} if there are no more tokens.
     * @throws CodecException if there is an error reading the token.
     */
    public boolean read() throws CodecException {
        taggedTokenValue = null;

        if (bufferedFaunaTokenType != null) {
            currentFaunaTokenType = bufferedFaunaTokenType;
            bufferedFaunaTokenType = null;
            if (closers.contains(currentFaunaTokenType)) {
                tokenStack.pop();
            }
            return true;
        }

        bufferedTokenValue = null;

        if (!advance()) {
            return false;
        }

        JsonToken currentToken = jsonParser.currentToken();
        if (currentToken != null) {
            switch (currentToken) {
                case VALUE_STRING:
                    currentFaunaTokenType = FaunaTokenType.STRING;
                    break;
                case START_OBJECT:
                    handleStartObject();
                    break;
                case START_ARRAY:
                    tokenStack.push(FaunaTokenType.START_ARRAY);
                    currentFaunaTokenType = FaunaTokenType.START_ARRAY;
                    break;
                case END_OBJECT:
                    handleEndObject();
                    break;
                case END_ARRAY:
                    tokenStack.pop();
                    currentFaunaTokenType = FaunaTokenType.END_ARRAY;
                    break;
                case VALUE_TRUE:
                    currentFaunaTokenType = FaunaTokenType.TRUE;
                    break;
                case VALUE_FALSE:
                    currentFaunaTokenType = FaunaTokenType.FALSE;
                    break;
                case FIELD_NAME:
                    currentFaunaTokenType = FaunaTokenType.FIELD_NAME;
                    break;
                case VALUE_NULL:
                    currentFaunaTokenType = FaunaTokenType.NULL;
                    break;
                default:
                    throw new CodecException(
                            "Unhandled JSON token type " + currentToken + ".");
            }
        } else {
            return false;
        }

        return true;
    }

    private void handleStartObject() throws CodecException {
        advanceTrue();
        switch (jsonParser.currentToken()) {
            case FIELD_NAME:
                switch (getText()) {
                    case BYTES_TAG:
                        handleTaggedString(FaunaTokenType.BYTES);
                        break;
                    case INT_TAG:
                        handleTaggedString(FaunaTokenType.INT);
                        break;
                    case DATE_TAG:
                        handleTaggedString(FaunaTokenType.DATE);
                        break;
                    case TIME_TAG:
                        handleTaggedString(FaunaTokenType.TIME);
                        break;
                    case DOUBLE_TAG:
                        handleTaggedString(FaunaTokenType.DOUBLE);
                        break;
                    case LONG_TAG:
                        handleTaggedString(FaunaTokenType.LONG);
                        break;
                    case MOD_TAG:
                        handleTaggedString(FaunaTokenType.MODULE);
                        break;
                    case STREAM_TAG:
                        handleTaggedString(FaunaTokenType.STREAM);
                        break;
                    case OBJECT_TAG:
                        advanceTrue();
                        currentFaunaTokenType = FaunaTokenType.START_OBJECT;
                        tokenStack.push(InternalTokenType.START_ESCAPED_OBJECT);
                        break;
                    case DOC_TAG:
                        advanceTrue();
                        currentFaunaTokenType = FaunaTokenType.START_DOCUMENT;
                        tokenStack.push(FaunaTokenType.START_DOCUMENT);
                        break;
                    case SET_TAG:
                        advanceTrue();
                        currentFaunaTokenType = FaunaTokenType.START_PAGE;
                        if (jsonParser.currentToken() == JsonToken.VALUE_STRING) {
                            bufferedFaunaTokenType = FaunaTokenType.STRING;

                            try {
                                bufferedTokenValue = jsonParser.getValueAsString();
                            } catch (IOException e) {
                                throw new CodecException(e.getMessage(), e);
                            }

                            tokenStack.push(
                                    InternalTokenType.START_PAGE_UNMATERIALIZED);
                        } else {
                            tokenStack.push(FaunaTokenType.START_PAGE);
                        }
                        break;
                    case REF_TAG:
                        advanceTrue();
                        currentFaunaTokenType = FaunaTokenType.START_REF;
                        tokenStack.push(FaunaTokenType.START_REF);
                        break;
                    default:
                        bufferedFaunaTokenType = FaunaTokenType.FIELD_NAME;
                        tokenStack.push(FaunaTokenType.START_OBJECT);
                        currentFaunaTokenType = FaunaTokenType.START_OBJECT;
                        break;
                }
                break;
            case END_OBJECT:
                bufferedFaunaTokenType = FaunaTokenType.END_OBJECT;
                tokenStack.push(FaunaTokenType.START_OBJECT);
                currentFaunaTokenType = FaunaTokenType.START_OBJECT;
                break;
            default:
                throw new CodecException(
                        "Unexpected token following StartObject: " + jsonParser.currentToken());
        }
    }

    private void handleEndObject() {
        Object startToken = tokenStack.pop();
        if (startToken.equals(FaunaTokenType.START_DOCUMENT)) {
            currentFaunaTokenType = FaunaTokenType.END_DOCUMENT;
            advanceTrue();
        } else if (startToken.equals(InternalTokenType.START_PAGE_UNMATERIALIZED)) {
            currentFaunaTokenType = FaunaTokenType.END_PAGE;
        } else if (startToken.equals(FaunaTokenType.START_PAGE)) {
            currentFaunaTokenType = FaunaTokenType.END_PAGE;
            advanceTrue();
        } else if (startToken.equals(FaunaTokenType.START_REF)) {
            currentFaunaTokenType = FaunaTokenType.END_REF;
            advanceTrue();
        } else if (startToken.equals(InternalTokenType.START_ESCAPED_OBJECT)) {
            currentFaunaTokenType = FaunaTokenType.END_OBJECT;
            advanceTrue();
        } else if (startToken.equals(FaunaTokenType.START_OBJECT)) {
            currentFaunaTokenType = FaunaTokenType.END_OBJECT;
        } else {
            throw new CodecException("Unexpected token " + startToken + ". This might be a bug.");
        }
    }

    private void handleTaggedString(final FaunaTokenType token) throws CodecException {
        try {
            advanceTrue();
            currentFaunaTokenType = token;
            taggedTokenValue = jsonParser.getText();
            advance();
        } catch (IOException exc) {
            throw CodecException.decodingIOException(exc);
        }
    }

    private String getText() throws CodecException {
        try {
            return jsonParser.getText();
        } catch (IOException exc) {
            throw CodecException.decodingIOException(exc);
        }
    }

    private void advanceTrue() {
        if (!advance()) {
            throw new CodecException(
                    "Unexpected end of underlying JSON reader.");
        }
    }

    private boolean advance() {
        try {
            return Objects.nonNull(jsonParser.nextToken());
        } catch (IOException e) {
            throw new CodecException(
                    "Failed to advance underlying JSON reader.", e);
        }
    }

    private void validateTaggedType(final FaunaTokenType type) {
        if (currentFaunaTokenType != type || taggedTokenValue == null) {
            throw new IllegalStateException(
                    "CurrentTokenType is a " + currentFaunaTokenType.toString() + ", not a " + type.toString() + ".");
        }
    }

    private void validateTaggedTypes(final FaunaTokenType... types) {
        if (!Arrays.asList(types).contains(currentFaunaTokenType)) {
            throw new IllegalStateException(
                    "CurrentTokenType is a " + currentFaunaTokenType.toString() + ", not in " + Arrays.toString(types) + ".");
        }
    }

    // Getters for various token types with appropriate validation

    /**
     * Retrieves the value as a {@code Character} if the current token type is {@link FaunaTokenType#INT}.
     *
     * @return The current value as a {@link Character}.
     */
    public Character getValueAsCharacter() {
        validateTaggedType(FaunaTokenType.INT);
        return (char) Integer.parseInt(taggedTokenValue);
    }

    /**
     * Retrieves the current value as a {@link String}.
     *
     * @return The current value as a {@link String}.
     */
    public String getValueAsString() {
        try {
            if (bufferedTokenValue != null) {
                return bufferedTokenValue.toString();
            }
            return jsonParser.getValueAsString();
        } catch (IOException e) {
            throw new CodecException(
                    "Error getting the current token as String", e);
        }
    }

    /**
     * Retrieves the tagged value as a {@link String}.
     *
     * @return The tagged value as a {@link String}.
     */
    public String getTaggedValueAsString() {
        return taggedTokenValue;
    }

    /**
     * Retrieves the value as a byte array if the current token type is {@link FaunaTokenType#BYTES}.
     *
     * @return The current value as a byte array.
     */
    public byte[] getValueAsByteArray() {
        validateTaggedTypes(FaunaTokenType.BYTES);
        return Base64.getDecoder().decode(taggedTokenValue.getBytes());
    }

    /**
     * Retrieves the value as a {@code Byte} if the current token type is {@link FaunaTokenType#INT}.
     *
     * @return The current value as a {@code Byte}.
     */
    public Byte getValueAsByte() {
        validateTaggedType(FaunaTokenType.INT);
        try {
            return Byte.parseByte(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new CodecException("Error getting the current token as Byte", e);
        }
    }

    /**
     * Retrieves the value as a {@code Short} if the current token type is {@link FaunaTokenType#INT}.
     *
     * @return The current value as a {@code Short}.
     */
    public Short getValueAsShort() {
        validateTaggedType(FaunaTokenType.INT);
        try {
            return Short.parseShort(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new CodecException("Error getting the current token as Short", e);
        }
    }

    /**
     * Retrieves the value as an {@code Integer} if the current token type is {@link FaunaTokenType#INT} or {@link FaunaTokenType#LONG}.
     *
     * @return The current value as an {@code Integer}.
     */
    public Integer getValueAsInt() {
        validateTaggedTypes(FaunaTokenType.INT, FaunaTokenType.LONG);
        try {
            return Integer.parseInt(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new CodecException("Error getting the current token as Integer", e);
        }
    }

    /**
     * Retrieves the current value as a {@code Boolean}.
     *
     * @return The current value as a {@code Boolean}.
     */
    public Boolean getValueAsBoolean() {
        try {
            return jsonParser.getValueAsBoolean();
        } catch (IOException e) {
            throw new CodecException("Error getting the current token as Boolean", e);
        }
    }

    /**
     * Retrieves the current value as a {@link LocalDate} if the current token type is {@link FaunaTokenType#DATE}.
     *
     * @return The current value as a {@link LocalDate}.
     */
    public LocalDate getValueAsLocalDate() {
        validateTaggedType(FaunaTokenType.DATE);
        try {
            return LocalDate.parse(taggedTokenValue);
        } catch (DateTimeParseException e) {
            throw new CodecException("Error getting the current token as LocalDate", e);
        }
    }

    /**
     * Retrieves the current value as an {@link Instant} if the current token type is {@link FaunaTokenType#TIME}.
     *
     * @return The current value as an {@link Instant}.
     */
    public Instant getValueAsTime() {
        validateTaggedType(FaunaTokenType.TIME);
        try {
            return Instant.parse(taggedTokenValue);
        } catch (DateTimeParseException e) {
            throw new CodecException("Error getting the current token as LocalDateTime", e);
        }
    }

    /**
     * Retrieves the value as a {@code Float} if the current token type is
     * {@link FaunaTokenType#INT}, {@link FaunaTokenType#LONG}, or {@link FaunaTokenType#DOUBLE}.
     *
     * @return The current value as a {@code Float}.
     */
    public Float getValueAsFloat() {
        validateTaggedTypes(FaunaTokenType.INT, FaunaTokenType.LONG, FaunaTokenType.DOUBLE);
        try {
            return Float.parseFloat(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new CodecException("Error getting the current token as Float", e);
        }
    }

    /**
     * Retrieves the value as a {@code Double} if the current token type is {@link FaunaTokenType#INT},
     * {@link FaunaTokenType#LONG}, or {@link FaunaTokenType#DOUBLE}.
     *
     * @return The current value as a {@code Double}.
     */
    public Double getValueAsDouble() {
        validateTaggedTypes(FaunaTokenType.INT, FaunaTokenType.LONG, FaunaTokenType.DOUBLE);
        try {
            return Double.parseDouble(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new CodecException("Error getting the current token as Double", e);
        }
    }

    /**
     * Retrieves the value as a {@code Long} if the current token type is
     * {@link FaunaTokenType#INT} or {@link FaunaTokenType#LONG}.
     *
     * @return The current value as a {@code Long}.
     */
    public Long getValueAsLong() {
        validateTaggedTypes(FaunaTokenType.INT, FaunaTokenType.LONG);
        try {
            return Long.parseLong(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new CodecException("Error getting the current token as Long", e);
        }
    }

    /**
     * Retrieves the value as a {@link Module} if the current token type is {@link FaunaTokenType#MODULE}.
     *
     * @return The current value as a {@link Module}.
     */
    public Module getValueAsModule() {
        try {
            return new Module(taggedTokenValue);
        } catch (Exception e) {
            throw new CodecException("Error getting the current token as Module", e);
        }
    }
}
