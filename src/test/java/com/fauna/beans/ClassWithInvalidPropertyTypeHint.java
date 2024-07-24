package com.fauna.beans;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;
import com.fauna.enums.FaunaType;

@FaunaObject
public class ClassWithInvalidPropertyTypeHint {

    @FaunaField(name = "first_name", type = FaunaType.INT)
    public String firstName = "NotANumber";
}