package com.fauna.encoding;

import com.fauna.query.model.BaseReference;
import com.fauna.query.model.DocumentReference;
import com.fauna.query.model.NamedDocumentReference;
import com.fauna.query.model.NullDocument;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper class for serializing {@code NullDocument} instances in the structure expected by Fauna.
 * This class adapts the {@code NullDocument} to a JSON-serializable format by converting its reference
 * information into a map structure. The resulting JSON object uses the "@ref" key to denote the reference
 * type according to Fauna's schema.
 */
class NullDocumentWrapper {

    /**
     * A map representing the reference details of a {@code NullDocument}.
     * The map is structured with keys that Fauna understands for representing document references.
     */
    @SerializedName("@ref")
    private final Map<String, Object> refMap;

    /**
     * Constructs a new {@code NullDocumentWrapper} with the specified {@code NullDocument}.
     * Depending on the subtype of the {@code BaseReference} held by the {@code NullDocument},
     * it prepares a map with either "id" and "coll" keys for a {@code DocumentReference} or "name"
     * and "coll" for a {@code NamedDocumentReference}.
     *
     * @param nullDoc The {@code NullDocument} to wrap. Its reference should be a valid {@code BaseReference}
     *                instance, either {@code DocumentReference} or {@code NamedDocumentReference}.
     *                The reference is used to populate the map with appropriate details.
     */
    public NullDocumentWrapper(NullDocument nullDoc) {
        this.refMap = new HashMap<>();
        BaseReference ref = nullDoc.getRef();

        // Check the type of reference and populate the map accordingly.
        if (ref instanceof DocumentReference docRef) {
            Map<String, Object> collMap = new HashMap<>();
            collMap.put("@mod", docRef.getColl().getName());
            refMap.put("id", docRef.getId());
            refMap.put("coll", collMap);
        } else if (ref instanceof NamedDocumentReference namedDocRef) {
            Map<String, Object> collMap = new HashMap<>();
            collMap.put("@mod", namedDocRef.getColl().getName());
            refMap.put("name", namedDocRef.getName());
            refMap.put("coll", collMap);
        }

    }

}
