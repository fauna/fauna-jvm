package com.fauna.beans;

import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;
import com.fauna.common.enums.FaunaType;

@ObjectAttribute
public class ClassWithInvalidPropertyTypeHint {

    @FieldAttribute(name = "first_name", type = FaunaType.INT)
    public String firstName = "NotANumber";
}