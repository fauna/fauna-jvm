package com.fauna.response.wire;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.json.PassThroughDeserializer;
import com.fauna.response.ConstraintFailure;

@Deprecated
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

    public ConstraintFailure toConstraintFailure() {
        ConstraintFailure.PathElement[][] paths = new ConstraintFailure.PathElement[1][1];
        // This is incorrect but it will be removed soon.
        paths[0][0] = new ConstraintFailure.PathElement(this.getPaths());
        return new ConstraintFailure(this.name, this.message, paths);

    }
}
