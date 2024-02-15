package com.fauna.beans;

import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;

@ObjectAttribute
public class ClassWithFieldAttributeAndWithoutObjectAttribute {

    @FieldAttribute(name = "first_name")
    public String firstName = "Baz";
}