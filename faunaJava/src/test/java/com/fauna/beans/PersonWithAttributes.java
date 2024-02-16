package com.fauna.beans;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;
import com.fauna.common.enums.FaunaType;

@FaunaObject
public class PersonWithAttributes {

    @FaunaField(name = "first_name")
    private String firstName;

    @FaunaField(name = "last_name")
    private String lastName;

    @FaunaField(name = "age", type = FaunaType.INT, nullable = true)
    private Integer age;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getAge() {
        return age;
    }

}
