package com.fauna.query.model;

import java.util.Objects;

/**
 * Represents a Module in the Fauna database system. A module may represent various
 * constructs within Fauna such as collections, mathematical modules, or user-defined collections.
 * The Module class provides a structure to interact with these different elements through
 * Fauna query language (FQL) by encapsulating the module's name.
 */
public class Module {

    /**
     * The name of the module.
     */
    private final String name;

    /**
     * Constructs a new Module with the specified name.
     * The name parameter represents the identifier used within FQL queries.
     *
     * @param name The name of the module to be created, not null.
     */
    public Module(String name) {
        this.name = name;
    }

    /**
     * Retrieves the name of the module.
     *
     * @return The name of the module.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Module module = (Module) o;

        return Objects.equals(name, module.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
