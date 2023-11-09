package com.fauna.encoding;

import com.fauna.query.model.NamedDocumentReference;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper class that prepares a {@link NamedDocumentReference} for JSON serialization with GSON,
 * formatting it according to Fauna's reference object structure. This class specifically handles
 * the inclusion of the document's name and its collection module within a serialized reference map.
 */
class NamedDocumentReferenceWrapper {

    /**
     * A map that holds the structure of a named document reference as expected by Fauna when serialized.
     * The serialized name and collection information are encapsulated within this map with the "@ref"
     * key annotation provided by the {@link SerializedName} annotation.
     */
    @SerializedName("@ref")
    private final Map<String, Object> reference;

    /**
     * Constructs a new {@code NamedDocumentReferenceWrapper} with the provided {@link NamedDocumentReference}.
     * This constructor initializes a map with "name" and "coll" keys to represent the document reference
     * as required by Fauna's JSON format for named references.
     *
     * @param namedDocumentReference The named document reference to wrap, not to be {@code null}.
     *                               It should contain both the name of the document and the collection
     *                               module reference.
     * @throws NullPointerException If the input {@code namedDocumentReference} or any of its required
     *                              properties are {@code null}.
     */
    public NamedDocumentReferenceWrapper(NamedDocumentReference namedDocumentReference) {
        if (namedDocumentReference == null) {
            throw new NullPointerException("NamedDocumentReference cannot be null.");
        }
        // Initialize the map to be used for serialization with appropriate keys and values.
        Map<String, Object> details = new HashMap<>();
        details.put("name", namedDocumentReference.getName());
        details.put("coll", new ModuleWrapper(namedDocumentReference.getColl()));
        this.reference = details;
    }
}

