package com.fauna.beans;


import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;


@ObjectAttribute
public class PersonWithObjectConflict {

    @FieldAttribute(name = "@object")
    public String field = "not";
}