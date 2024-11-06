package com.fauna.beans;

import java.util.Objects;

public class ClassWithInheritanceL2 extends ClassWithInheritance {


    public ClassWithInheritanceL2(String firstName, String lastName, int age) {
        super(firstName, lastName, age);
    }

    public ClassWithInheritanceL2() {
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

        ClassWithInheritanceL2 c = (ClassWithInheritanceL2) o;

        return Objects.equals(getFirstName(), c.getFirstName())
                && Objects.equals(getLastName(), c.getLastName())
                && Objects.equals(getAge(), c.getAge());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getAge());
    }
}
