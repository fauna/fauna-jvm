package com.fauna.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.codec.json.PassThroughDeserializer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ConstraintFailure {
    @JsonProperty("message")
    private String message;

    @JsonProperty("name")
    private String name;

    @JsonProperty("paths")
    @JsonDeserialize(using = PassThroughDeserializer.class)
    private String pathsRaw;

    private Optional<List<List<Object>>> pathsDecoded;

    public String getMessage() {
        return this.message;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    /**
     * Each path returned by Fauna for a constraint failure is an array of strings and integers. But since Java
     * doesn't really have a way to support union types, returning Object (the common parent of String and Integer)
     * seems like the simplest solution.
     *
     * @return
     */
    public Optional<List<List<Object>>> getPaths() throws IOException {
        if (pathsDecoded != null) return pathsDecoded;

        var codec = DefaultCodecProvider.SINGLETON.get(Object.class);
        var parser = new UTF8FaunaParser(this.pathsRaw);
        var result = codec.decode(parser);

        if (result == null) {
            pathsDecoded = Optional.empty();
        } else {
            pathsDecoded = Optional.ofNullable((List<List<Object>>) result);
        }

        return pathsDecoded;
    }

}
