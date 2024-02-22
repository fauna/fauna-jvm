package com.fauna.beans;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;

@FaunaObject
public class ClassWithFieldAttributeAndWithoutObjectAttribute {

    @FaunaField(name = "first_name")
    public String firstName = "Baz";
}