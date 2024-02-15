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

    @FieldAttribute(name = "age", type = FaunaType.INT)
    private int age;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

}