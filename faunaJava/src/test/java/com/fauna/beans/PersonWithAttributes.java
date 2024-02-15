package com.fauna.beans;

import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;
import com.fauna.common.enums.FaunaType;

@ObjectAttribute
public class PersonWithAttributes {

    @FieldAttribute(name = "first_name")
    private String firstName;

    @FieldAttribute(name = "last_name")
    private String lastName;

    @FieldAttribute(name = "age", type = FaunaType.LONG)
    private long age;

    public PersonWithAttributes(String firstName, String lastName, long age) {
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

    public long getAge() {
        return age;
    }

}
