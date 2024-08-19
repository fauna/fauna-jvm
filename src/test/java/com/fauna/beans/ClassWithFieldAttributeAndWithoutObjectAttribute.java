package com.fauna.beans;

import com.fauna.annotation.FaunaField;

public class ClassWithFieldAttributeAndWithoutObjectAttribute {

    @FaunaField(name = "first_name")
    public String firstName = "Baz";
}