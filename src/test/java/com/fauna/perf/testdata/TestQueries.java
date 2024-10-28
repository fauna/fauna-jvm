package com.fauna.perf.testdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

// Define TestQueries (container for TestQuery objects)
public class TestQueries {
    @JsonProperty("queries")
    private List<TestQuery> queries = new ArrayList<>();

    // Getter and setter
    public List<TestQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<TestQuery> queries) {
        this.queries = queries;
    }
}
