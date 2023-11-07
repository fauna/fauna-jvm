package com.fauna.encoding;

import com.fauna.query.model.Module;
import com.google.gson.annotations.SerializedName;

/**
 * Wraps a module object for JSON serialization with GSON. This class specifically handles
 * the serialization of {@link Module} instances to conform with the JSON structure expected by
 * Fauna. The serialization process will output a JSON object with a single key "@mod" that
 * maps to the name of the module.
 */
class ModuleWrapper {

    /**
     * The name of the module represented as a string. The {@link SerializedName} annotation
     * specifies the key "@mod" under which this value will be placed in the serialized JSON object.
     */
    @SerializedName("@mod")
    private final String name;

    /**
     * Constructs a {@code ModuleWrapper} with the provided {@link Module} object.
     * It extracts the name of the module and prepares it for serialization with the "@mod" key.
     *
     * @param module The {@link Module} instance to wrap. The module name is extracted and stored.
     *               Must not be {@code null} to avoid a {@link NullPointerException}.
     * @throws NullPointerException If the {@code module} parameter is {@code null}.
     */
    ModuleWrapper(Module module) {
        if (module == null) {
            throw new NullPointerException("Module cannot be null for ModuleWrapper.");
        }
        this.name = module.getName();
    }
}
