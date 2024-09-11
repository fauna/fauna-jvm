package com.fauna.beans;

import java.util.Objects;

public class ClassWithInheritance extends ClassWithAttributes {


    public ClassWithInheritance(String firstName, String lastName, int age) {
        super(firstName, lastName, age);
    }

    public ClassWithInheritance() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        ClassWithInheritance c = (ClassWithInheritance) o;

        return Objects.equals(getFirstName(), c.getFirstName())
                && Objects.equals(getLastName(), c.getLastName())
                && Objects.equals(getAge(), c.getAge());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getAge());
    }
}
