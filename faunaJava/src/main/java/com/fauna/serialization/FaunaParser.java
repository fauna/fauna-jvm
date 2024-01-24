package com.fauna.serialization;

import static com.fauna.common.enums.FaunaTokenType.END_ARRAY;
import static com.fauna.common.enums.FaunaTokenType.END_DOCUMENT;
import static com.fauna.common.enums.FaunaTokenType.END_OBJECT;
import static com.fauna.common.enums.FaunaTokenType.END_PAGE;
import static com.fauna.common.enums.FaunaTokenType.END_REF;
import static com.fauna.common.enums.FaunaTokenType.NONE;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fauna.common.enums.FaunaTokenType;
import com.fauna.exception.SerializationException;
import java.io.IOException;
import java.io.InputStream;
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
    private final Stack<Object> tokenStack = new Stack<>();
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

    public JsonToken getCurrentTokenType() {
        return jsonParser.currentToken();
    }

    private final Set<FaunaTokenType> closers = new HashSet<>(Arrays.asList(
        END_OBJECT,
        END_PAGE,
        END_DOCUMENT,
        END_REF,
        END_ARRAY
    ));

    public boolean read() {
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
                default:
                    throw new SerializationException(
                        "Unhandled JSON token type " + currentToken + ".");
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean advance() {
        try {
            return Objects.nonNull(jsonParser.nextToken());
        } catch (IOException e) {
            throw new SerializationException("Failed to advance underlying JSON reader.", e);
        }
    }

    public String getValueAsString() {
        try {
            return jsonParser.getValueAsString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading current token as String", e);
        }
    }
}