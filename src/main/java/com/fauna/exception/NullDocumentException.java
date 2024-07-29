package com.fauna.exception;

import com.fauna.types.Module;

public class NullDocumentException extends FaunaException {

    private String id;
    private Module coll;
    private String nullCause;

    public String getId() {
        return id;
    }

    public Module getCollection() {
        return coll;
    }

    public String getNullCause() {
        return nullCause;
    }

    public NullDocumentException(String id, Module coll, String nullCause) {
        super(String.format("Document %s in collection %s is null: %s", id, coll != null ? coll.getName() : "unknown", nullCause));
        this.id = id;
        this.coll = coll;
        this.nullCause = nullCause;
    }
}
