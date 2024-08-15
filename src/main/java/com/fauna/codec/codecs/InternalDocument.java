package com.fauna.codec.codecs;

import com.fauna.enums.FaunaTokenType;
import com.fauna.exception.NullDocumentException;
import com.fauna.serialization.UTF8FaunaParser;
import com.fauna.types.*;
import com.fauna.types.Module;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

class InternalDocument {

    static class Builder {

        private String id = null;
        private String name = null;
        private Module coll = null;
        private Boolean exists = null;
        private String cause = null;
        private Instant ts = null;
        private final Map<String, Object> data = new HashMap<>();
        private boolean throwIfNotExists = true;

        InternalDocument.Builder withDataField(String key, Object value) {
            data.put(key, value);
            return this;
        }

        InternalDocument.Builder withDocField(String fieldName, UTF8FaunaParser parser) {
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
                case "ts":
                    if (parser.getCurrentTokenType() == FaunaTokenType.TIME) {
                        this.ts = parser.getValueAsTime();
                    }
            }
            return this;
        }

        InternalDocument.Builder withRefField(String fieldName, UTF8FaunaParser parser) {
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
                    if (parser.getCurrentTokenType() == FaunaTokenType.FALSE || parser.getCurrentTokenType() == FaunaTokenType.TRUE) {
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

        Object build() {
            if (exists != null && !exists && throwIfNotExists) {
                throw new NullDocumentException(id != null ? id : name, coll, cause);
            }

            if (id != null && coll != null && ts != null) {
                if (name != null) {
                    data.put("name", name);
                }
                return new Document(id, coll, ts, data);
            }

            if (id != null && coll != null) {
                return new DocumentRef(id, coll);
            }

            if (name != null && coll != null && ts != null) {
                return new NamedDocument(name, coll, ts, data);
            }

            if (name != null && coll != null) {
                return new NamedDocumentRef(name, coll);
            }

            // We got something we don't know how to handle, so just return it.
            if (id != null) {
                data.put("id", id);
            }

            if (name != null) {
                data.put("name", name);
            }

            if (coll != null) {
                data.put("coll", coll);
            }

            if (ts != null) {
                data.put("ts", ts);
            }

            if (exists != null) {
                data.put("exists", exists);
            }

            if (cause != null) {
                data.put("cause", cause);
            }

            return data;
        }
    }
}
