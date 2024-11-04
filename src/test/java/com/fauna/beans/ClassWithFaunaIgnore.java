package com.fauna.beans;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaIgnore;

import java.util.Objects;

public class ClassWithFaunaIgnore {
    @FaunaField(name = "first_name")
    private String firstName;

    @FaunaField(name = "last_name")
    private String lastName;

    @FaunaIgnore
    private Integer age;

    public ClassWithFaunaIgnore(String firstName, String lastName,
                                Integer age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public ClassWithFaunaIgnore() {

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
      if (this == o) {
        return true;
      }

      if (o == null) {
        return false;
      }

      if (getClass() != o.getClass()) {
        return false;
      }

        ClassWithFaunaIgnore c = (ClassWithFaunaIgnore) o;

        return firstName.equals(c.firstName)
                && lastName.equals(c.lastName)
                && Objects.equals(age, c.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, age);
    }
}
