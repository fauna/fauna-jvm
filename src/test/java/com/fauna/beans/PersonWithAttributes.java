package com.fauna.beans;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;

import java.util.Objects;

@FaunaObject
public class PersonWithAttributes {

    @FaunaField(name = "first_name")
    private String firstName;

    @FaunaField(name = "last_name")
    private String lastName;

    @FaunaField(name = "age", nullable = true)
    private Integer age;

    public PersonWithAttributes(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public PersonWithAttributes() {

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getAge() {
        return age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        PersonWithAttributes c = (PersonWithAttributes) o;

        return firstName.equals(c.firstName)
                && lastName.equals(c.lastName)
                && Objects.equals(age, c.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, age);
    }
}