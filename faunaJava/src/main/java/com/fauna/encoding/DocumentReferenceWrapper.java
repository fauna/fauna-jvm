package com.fauna.encoding;

import com.fauna.query.model.DocumentReference;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps a Fauna document reference for serialization with GSON.
 * This class is a utility to convert {@link DocumentReference} instances into the JSON format
 * used by Fauna to denote references, with specific structure dictated by the database's
 * requirements for references.
 */
class DocumentReferenceWrapper {

    /**
     * A map representing the structure of a Fauna reference.
     * The map is serialized into a JSON object with the key "@ref" to conform with
     * the JSON format that Fauna expects for document references.
     */
    @SerializedName("@ref")
    private final Map<String, Object> ref;

    /**
     * Constructs a new {@code DocumentReferenceWrapper} with the specified {@link DocumentReference}.
     * It initializes an internal map structure to hold and represent the reference in the way
     * Fauna can interpret.
     *
     * @param documentReference The document reference to wrap. This object must contain the ID
     *                          and the collection module information to construct a valid reference.
     *                          It should not be {@code null}.
     * @throws NullPointerException If {@code documentReference} or any required property of it is {@code null}.
     */
    public DocumentReferenceWrapper(DocumentReference documentReference) {
        if (documentReference == null) {
            throw new NullPointerException("DocumentReference cannot be null.");
        }

        // Initialize the reference map with the document's ID and collection information.
        this.ref = new HashMap<>();
        this.ref.put("id", documentReference.getId());
        this.ref.put("coll", new ModuleWrapper(documentReference.getColl()));
    }

}
