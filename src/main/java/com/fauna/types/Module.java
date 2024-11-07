package com.fauna.types;

import java.util.Objects;

/**
 * Represents a module in Fauna Query Language (FQL), which serves as a symbolic object
 * with associated methods. Modules can represent built-in FQL objects, such as "Collection" or "Math",
 * as well as user-defined entities like custom collections.
 * <p>
 * For example, a specific collection named "MyCollection" can be represented as a {@code Module} in Java
 * to enable document creation via the Fauna Java driver:
 * <pre>
 *     var q = fql(
 *         "${coll}.create({foo:'bar'})",
 *         Map.of("coll", new Module("MyCollection"))
 *     );
 *
 *     client.query(q);
 * </pre>
 */
public final class Module {

    private final String name;

    /**
     * Constructs a new {@code Module} object with the specified name.
     *
     * @param name The name of the module, representing either a built-in FQL object or a user-defined collection.
     */
    public Module(final String name) {
        this.name = name;
    }

    /**
     * Gets the name of this module as a string representation.
     *
     * @return A {@code String} representing the module's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Determines if this module is equal to another object. Two modules are considered equal
     * if they have the same name.
     *
     * @param obj The object to compare with this module for equality.
     * @return {@code true} if the specified object is equal to this module; otherwise, {@code false}.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Module module = (Module) obj;
        return Objects.equals(name, module.name);
    }

    /**
     * Returns a hash code value for this module based on its name.
     *
     * @return An integer hash code for this module.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
