package com.fauna.response.wire;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.json.PassThroughDeserializer;

import java.util.Optional;

public class ConstraintFailureWire {
    @JsonProperty("message")
    private String message;

    @JsonProperty("name")
    private String name;

    @JsonProperty("paths")
    @JsonDeserialize(using = PassThroughDeserializer.class)
    private String paths;


    public String getMessage() {
        return this.message;
    }

    public String getName() {
        return this.name;
    }

    public String getPaths() {
        return paths;
    }
}
