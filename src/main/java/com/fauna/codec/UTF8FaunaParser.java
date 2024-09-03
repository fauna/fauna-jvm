package com.fauna.codec;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.enums.FaunaTokenType;
import com.fauna.types.Module;
import com.fauna.exception.ClientException;
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

import static com.fauna.enums.FaunaTokenType.*;

/**
 * Represents a reader that provides fast, non-cached, forward-only access to serialized data.
 */
public class UTF8FaunaParser {

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
    private FaunaTokenType currentFaunaTokenType = NONE;
    private FaunaTokenType bufferedFaunaTokenType;
    private String taggedTokenValue;

    public UTF8FaunaParser(String str) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        JsonFactory factory = new JsonFactory();
        this.jsonParser = factory.createParser(inputStream);
        if (getCurrentTokenType() == FaunaTokenType.NONE) {
            read();
        }
    }

    private enum InternalTokenType {
        START_ESCAPED_OBJECT
    }

    public UTF8FaunaParser(InputStream body) throws IOException {
        JsonFactory factory = new JsonFactory();
        this.jsonParser = factory.createParser(body);
        if (getCurrentTokenType() == FaunaTokenType.NONE) {
            read();
        }
    }

    public UTF8FaunaParser(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    public FaunaTokenType getCurrentTokenType() {
        return currentFaunaTokenType;
    }

    private final Set<FaunaTokenType> closers = new HashSet<>(Arrays.asList(
        END_OBJECT,
        END_PAGE,
        END_DOCUMENT,
        END_REF,
        END_ARRAY
    ));

    public void skip() throws IOException {
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

    private void skipInternal() throws IOException {
        int startCount = tokenStack.size();
        while (read()) {
            if (tokenStack.size() < startCount) {
                break;
            }
        }
    }

    public boolean read() throws IOException {
        taggedTokenValue = null;

        if (bufferedFaunaTokenType != null) {
            currentFaunaTokenType = bufferedFaunaTokenType;
            bufferedFaunaTokenType = null;
            if (closers.contains(currentFaunaTokenType)) {
                tokenStack.pop();
            }
            return true;
        }

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
                    throw new ClientException(
                        "Unhandled JSON token type " + currentToken + ".");
            }
        } else {
            return false;
        }

        return true;
    }

    private void handleStartObject() throws IOException {
        advanceTrue();

        switch (jsonParser.currentToken()) {
            case FIELD_NAME:
                switch (jsonParser.getText()) {
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
                        tokenStack.push(FaunaTokenType.START_PAGE);
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
                throw new ClientException(
                    "Unexpected token following StartObject: " + jsonParser.currentToken());
        }
    }

    private void handleEndObject() {
        Object startToken = tokenStack.pop();
        if (startToken.equals(FaunaTokenType.START_DOCUMENT)) {
            currentFaunaTokenType = END_DOCUMENT;
            advanceTrue();
        } else if (startToken.equals(FaunaTokenType.START_PAGE)) {
            currentFaunaTokenType = END_PAGE;
            advanceTrue();
        } else if (startToken.equals(FaunaTokenType.START_REF)) {
            currentFaunaTokenType = END_REF;
            advanceTrue();
        } else if (startToken.equals(InternalTokenType.START_ESCAPED_OBJECT)) {
            currentFaunaTokenType = END_OBJECT;
            advanceTrue();
        } else if (startToken.equals(FaunaTokenType.START_OBJECT)) {
            currentFaunaTokenType = END_OBJECT;
        } else {
            throw new ClientException(
                "Unexpected token " + startToken + ". This might be a bug.");
        }
    }

    private void handleTaggedString(FaunaTokenType token) throws IOException {
        advanceTrue();
        currentFaunaTokenType = token;
        taggedTokenValue = jsonParser.getText();
        advance();
    }

    private void advanceTrue() {
        if (!advance()) {
            throw new ClientException("Unexpected end of underlying JSON reader.");
        }
    }

    private boolean advance() {
        try {
            return Objects.nonNull(jsonParser.nextToken());
        } catch (IOException e) {
            throw new ClientException("Failed to advance underlying JSON reader.", e);
        }
    }

    private void validateTaggedType(FaunaTokenType type) {
        if (currentFaunaTokenType != type || taggedTokenValue == null
            || !(taggedTokenValue instanceof String)) {
            throw new IllegalStateException(
                "CurrentTokenType is a " + currentFaunaTokenType.toString() +
                    ", not a " + type.toString() + ".");
        }
    }

    private void validateTaggedTypes(FaunaTokenType... types) {
        if (!Arrays.asList(types).contains(currentFaunaTokenType))
            throw new IllegalStateException(
                    "CurrentTokenType is a " + currentFaunaTokenType.toString() +
                            ", not in " + Arrays.toString(types) + ".");
    }

    public Character getValueAsCharacter() {
        validateTaggedType(INT);
        return Character.valueOf((char) Integer.parseInt(taggedTokenValue));
    }

    public String getValueAsString() {
        try {
            return jsonParser.getValueAsString();
        } catch (IOException e) {
            throw new ClientException("Error getting the current token as String", e);
        }
    }

    public String getTaggedValueAsString() {
        return taggedTokenValue;
    }

    public byte[] getValueAsByteArray() {
        validateTaggedTypes(BYTES);
        return Base64.getDecoder().decode(taggedTokenValue.getBytes());
    }

    public Byte getValueAsByte() {
        validateTaggedType(INT);
        try {
            return Byte.parseByte(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new ClientException("Error getting the current token as Byte", e);
        }

    }
    public Short getValueAsShort() {
        validateTaggedType(INT);
        try {
            return Short.parseShort(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new ClientException("Error getting the current token as Short", e);
        }

    }

    public Integer getValueAsInt() {
        validateTaggedTypes(INT, FaunaTokenType.LONG);
        try {
            return Integer.parseInt(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new ClientException("Error getting the current token as Integer", e);
        }
    }

    public Boolean getValueAsBoolean() {
        try {
            return jsonParser.getValueAsBoolean();
        } catch (IOException e) {
            throw new ClientException("Error getting the current token as Boolean", e);
        }
    }

    public LocalDate getValueAsLocalDate() {
        validateTaggedType(DATE);
        try {
            return LocalDate.parse(taggedTokenValue);
        } catch (DateTimeParseException e) {
            throw new ClientException("Error getting the current token as LocalDate", e);
        }
    }

    public Instant getValueAsTime() {
        validateTaggedType(TIME);
        try {
            return Instant.parse(taggedTokenValue);
        } catch (DateTimeParseException e) {
            throw new ClientException("Error getting the current token as LocalDateTime", e);
        }
    }

    public Float getValueAsFloat() {
        validateTaggedTypes(INT, LONG, DOUBLE);
        try {
            return Float.parseFloat(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new ClientException("Error getting the current token as Float", e);
        }
    }

    public Double getValueAsDouble() {
        validateTaggedTypes(INT, LONG, DOUBLE);
        try {
            return Double.parseDouble(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new ClientException("Error getting the current token as Double", e);
        }
    }

    public Long getValueAsLong() {
        validateTaggedTypes(INT, LONG);
        try {
            return Long.parseLong(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new ClientException("Error getting the current token as Long", e);
        }
    }

    public Module getValueAsModule() {
        try {
            return new Module(taggedTokenValue);
        } catch (Exception e) {
            throw new ClientException("Error getting the current token as Module", e);
        }
    }
}