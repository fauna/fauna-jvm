package com.fauna.serialization;

import static com.fauna.common.enums.FaunaTokenType.DATE;
import static com.fauna.common.enums.FaunaTokenType.DOUBLE;
import static com.fauna.common.enums.FaunaTokenType.END_ARRAY;
import static com.fauna.common.enums.FaunaTokenType.END_DOCUMENT;
import static com.fauna.common.enums.FaunaTokenType.END_OBJECT;
import static com.fauna.common.enums.FaunaTokenType.END_PAGE;
import static com.fauna.common.enums.FaunaTokenType.END_REF;
import static com.fauna.common.enums.FaunaTokenType.LONG;
import static com.fauna.common.enums.FaunaTokenType.NONE;
import static com.fauna.common.enums.FaunaTokenType.TIME;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.common.enums.FaunaTokenType;
import com.fauna.common.types.Module;
import com.fauna.exception.SerializationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

/**
 * Represents a reader that provides fast, non-cached, forward-only access to serialized data.
 */
public class FaunaParser {

    private static final String INT_TAG = "@int";
    private static final String LONG_TAG = "@long";
    private static final String DOUBLE_TAG = "@double";
    private static final String TIME_TAG = "@time";
    private static final String DATE_TAG = "@date";
    private static final String REF_TAG = "@ref";
    private static final String DOC_TAG = "@doc";
    private static final String MOD_TAG = "@mod";
    private static final String SET_TAG = "@set";
    private static final String OBJECT_TAG = "@object";//TODO Understand Module
    private final JsonParser jsonParser;
    private final Stack<FaunaTokenType> tokenStack = new Stack<>();
    private FaunaTokenType currentFaunaTokenType;
    private FaunaTokenType bufferedFaunaTokenType;
    private String taggedTokenValue;

    public FaunaParser(InputStream body) throws IOException {
        JsonFactory factory = new JsonFactory();
        this.jsonParser = factory.createParser(body);
        currentFaunaTokenType = NONE;
    }

    public FaunaParser(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
        currentFaunaTokenType = NONE;
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
                case NOT_AVAILABLE:
                    break;
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
                default:
                    throw new SerializationException(
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
                    case OBJECT_TAG:
                        advanceTrue();
                        currentFaunaTokenType = FaunaTokenType.START_OBJECT;
                        tokenStack.push(FaunaTokenType.START_ESCAPED_OBJECT);
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
                throw new SerializationException(
                    "Unexpected token following StartObject: " + jsonParser.currentToken());
        }
    }

    private void handleEndObject() {
        FaunaTokenType startToken = tokenStack.pop();
        switch (startToken) {
            case START_DOCUMENT:
                currentFaunaTokenType = END_DOCUMENT;
                advanceTrue();
                break;
            case START_PAGE:
                currentFaunaTokenType = END_PAGE;
                advanceTrue();
                break;
            case START_REF:
                currentFaunaTokenType = END_REF;
                advanceTrue();
                break;
            case START_ESCAPED_OBJECT:
                currentFaunaTokenType = END_OBJECT;
                advanceTrue();
                break;
            case START_OBJECT:
                currentFaunaTokenType = END_OBJECT;
                break;
            default:
                throw new SerializationException(
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
            throw new SerializationException("Unexpected end of underlying JSON reader.");
        }
    }

    private boolean advance() {
        try {
            return Objects.nonNull(jsonParser.nextToken());
        } catch (IOException e) {
            throw new SerializationException("Failed to advance underlying JSON reader.", e);
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

    public String getValueAsString() {
        try {
            return jsonParser.getValueAsString();
        } catch (IOException e) {
            throw new SerializationException("Error getting the current token as String", e);
        }
    }

    public Integer getValueAsInt() {
        validateTaggedType(FaunaTokenType.INT);
        try {
            return Integer.parseInt(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new SerializationException("Error getting the current token as Integer", e);
        }
    }

    public Boolean getValueAsBoolean() {
        try {
            return jsonParser.getValueAsBoolean();
        } catch (IOException e) {
            throw new SerializationException("Error getting the current token as Boolean", e);
        }
    }

    public LocalDate getValueAsLocalDate() {
        validateTaggedType(DATE);
        try {
            return LocalDate.parse(taggedTokenValue);
        } catch (DateTimeParseException e) {
            throw new SerializationException("Error getting the current token as LocalDate", e);
        }
    }

    public Instant getValueAsTime() {
        validateTaggedType(TIME);
        try {
            return Instant.parse(taggedTokenValue);
        } catch (DateTimeParseException e) {
            throw new SerializationException("Error getting the current token as LocalDateTime", e);
        }
    }

    public Double getValueAsDouble() {
        validateTaggedType(DOUBLE);
        try {
            return Double.parseDouble(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new SerializationException("Error getting the current token as Double", e);
        }
    }

    public Long getValueAsLong() {
        validateTaggedType(LONG);
        try {
            return Long.parseLong(taggedTokenValue);
        } catch (NumberFormatException e) {
            throw new SerializationException("Error getting the current token as Long", e);
        }
    }

    public Module getValueAsModule() {
        try {
            return new Module(taggedTokenValue);
        } catch (Exception e) {
            throw new SerializationException("Error getting the current token as Module", e);
        }
    }
}