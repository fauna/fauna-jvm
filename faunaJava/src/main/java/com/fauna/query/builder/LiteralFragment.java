package com.fauna.query.builder;

import java.util.Objects;

/**
 * Represents a literal fragment of a Fauna query.
 * This class encapsulates a fixed string that does not contain any variables.
 */
public class LiteralFragment extends Fragment {

    private final String value;

    /**
     * Constructs a new {@code LiteralFragment} with the given literal value.
     *
     * @param value the string value of this fragment; must not be null.
     * @throws IllegalArgumentException if {@code value} is null.
     */
    LiteralFragment(String value) {
        if (value == null) {
            throw new IllegalArgumentException("A literal value must not be null");
        }
        this.value = value;
    }

    /**
     * Retrieves the string value of this fragment.
     *
     * @return the string value.
     */
    @Override
    public String get() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LiteralFragment that = (LiteralFragment) o;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

}
