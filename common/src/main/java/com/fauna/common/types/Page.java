package com.fauna.common.types;

import java.util.List;

/**
 * Represents a page in a dataset for pagination.
 *
 * @param <T> The type of data contained in the page.
 */
public record Page<T>(List<T> data, String after) {

    @Override
    public List<T> data() {
        return data;
    }

    @Override
    public String after() {
        return after;
    }
}