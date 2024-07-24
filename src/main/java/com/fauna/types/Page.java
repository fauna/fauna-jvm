package com.fauna.common.types;

import java.util.List;

/**
 * Represents a page in a dataset for pagination.
 *
 * @param <T> The type of data contained in the page.
 */
public class Page<T>{
    private final List<T> data;
    private final String after;

    public Page(List<T> data, String after) {
        this.data = data;
        this.after = after;
    }

    public List<T> data() {
        return data;
    }

    public String after() {
        return after;
    }
}