package com.fauna.codec.codecs;

import com.fauna.client.ExponentialBackoffStrategy;
import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.NullDocumentException;
import com.fauna.serialization.UTF8FaunaParser;
import com.fauna.types.Module;

class InternalRef {

    private final String id;
    private final String name;
    private final Module coll;
    private final boolean exists;
    private final String cause;

    public InternalRef(String id, String name, Module coll, boolean exists, String cause){
        this.id = id;
        this.name = name;
        this.coll = coll;
        this.exists = exists;
        this.cause = cause;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Module getColl() {
        return coll;
    }

    public boolean getExists() {
        return exists;
    }

    public String getCause() {
        return cause;
    }

    public void throwIfNotExists() {
        if (!getExists()) {
            throw new NullDocumentException(getId() != null ? getId() : getName(), getColl(), getCause());
        }
    }

    static class Builder {
        private String id = null;
        private String name = null;
        private Module coll = null;
        private boolean exists = true;
        private String cause = null;

        InternalRef.Builder withField(String fieldName, UTF8FaunaParser parser) {
            switch (fieldName) {
                case "id":
                    if (parser.getCurrentTokenType() == FaunaTokenType.STRING) {
                        this.id = parser.getValueAsString();
                    }
                    break;
                case "name":
                    if (parser.getCurrentTokenType() == FaunaTokenType.STRING) {
                        this.name = parser.getValueAsString();
                    }
                    break;
                case "coll":
                    if (parser.getCurrentTokenType() == FaunaTokenType.MODULE) {
                        this.coll = parser.getValueAsModule();
                    }
                    break;
                case "exists":
                    if (parser.getCurrentTokenType() == FaunaTokenType.FALSE) {
                        this.exists = parser.getValueAsBoolean();
                    }
                    break;
                case "cause":
                    if (parser.getCurrentTokenType() == FaunaTokenType.STRING) {
                        this.cause = parser.getValueAsString();
                    }
                    break;
            }
            return this;
        }

        InternalRef build() {
            return new InternalRef(this.id, this.name, this.coll, this.exists, this.cause);
        }
    }
}
