package com.fauna.query.builder;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * Represents a literal fragment of a Fauna query.
 * This class encapsulates a fixed string that does not contain any variables.
 * A {@code QueryLiteral} is used to represent literal values in a query.
 */
@SuppressWarnings("rawtypes")
public final class QueryLiteral extends QueryFragment {

    private final String value;

    /**
     * Constructs a new {@code QueryLiteral} with the given literal value.
     *
     * @param value the string value of this fragment; must not be null.
     * @throws IllegalArgumentException if {@code value} is null.
     */
    public QueryLiteral(final String value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "A literal value must not be null");
        }
        this.value = value;
    }

    /**
     * Retrieves the string value of this literal fragment.
     *
     * @return the string value of this fragment.
     */
    @Override
    public String get() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryLiteral that = (QueryLiteral) o;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Gets the wrapped literal value.
     *
     * @return The literal value as a string.
     */
    @JsonValue
    public String getValue() {
        return this.value;
    }
}
