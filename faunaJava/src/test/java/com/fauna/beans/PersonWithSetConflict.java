package com.fauna.beans;


import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;


@ObjectAttribute
public class PersonWithSetConflict {

    @FieldAttribute(name = "@set")
    public String field = "not";
}