package com.fauna.types;

import java.util.Objects;

/**
 * Represents a document ref that has a "name" instead of an "id". For example, a Role document
 * reference is represented as a NamedRef.
 */
public class NamedDocumentRef extends BaseRef {

    private final String name;

    /**
     * Constructs a new NamedRef object with the specified name and collection.
     *
     * @param name The string value of the named document ref name.
     * @param coll The module to which the named document ref belongs.
     */
    public NamedDocumentRef(String name, Module coll) {
        super(coll);
        this.name = name;
    }

    /**
     * Gets the string value of the ref name.
     *
     * @return The string value of the ref name.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null) {
        return false;
      }

      if (getClass() != o.getClass()) {
        return false;
      }

        NamedDocumentRef c = (NamedDocumentRef) o;

        return name.equals(c.name)
                && getCollection().equals(c.getCollection());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, getCollection());
    }
}
