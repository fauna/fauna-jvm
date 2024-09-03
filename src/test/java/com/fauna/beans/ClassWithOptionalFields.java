package com.fauna.beans;

import com.fauna.annotation.FaunaField;

import java.util.Objects;
import java.util.Optional;

public class ClassWithOptionalFields {

    private String firstName;

    private Optional<String> lastName;

    public ClassWithOptionalFields(String firstName, Optional<String> lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public ClassWithOptionalFields() {

    }

    public String getFirstName() {
        return firstName;
    }

    public Optional<String> getLastName() {
        return lastName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        ClassWithOptionalFields c = (ClassWithOptionalFields) o;

        return Objects.equals(firstName, c.firstName)
                && Objects.equals(lastName, c.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }
}
