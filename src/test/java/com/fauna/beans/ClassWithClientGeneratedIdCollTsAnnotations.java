package com.fauna.beans;

import com.fauna.annotation.FaunaColl;
import com.fauna.annotation.FaunaId;
import com.fauna.annotation.FaunaTs;
import com.fauna.types.Module;

import java.time.Instant;
import java.util.Objects;

public class ClassWithClientGeneratedIdCollTsAnnotations {

    @FaunaId(isClientGenerate = true)
    private String id;
    @FaunaColl
    private Module coll;
    @FaunaTs
    private Instant ts;

    private String firstName;

    private String lastName;

    public ClassWithClientGeneratedIdCollTsAnnotations(String id, Module coll,
                                                       Instant ts,
                                                       String firstName,
                                                       String lastName) {
        this.id = id;
        this.coll = coll;
        this.ts = ts;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public ClassWithClientGeneratedIdCollTsAnnotations() {

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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

        ClassWithClientGeneratedIdCollTsAnnotations c =
                (ClassWithClientGeneratedIdCollTsAnnotations) o;

        return Objects.equals(id, c.id)
                && Objects.equals(coll, c.coll)
                && Objects.equals(ts, c.ts)
                && Objects.equals(firstName, c.firstName)
                && Objects.equals(lastName, c.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, coll, ts, firstName, lastName);
    }
}
