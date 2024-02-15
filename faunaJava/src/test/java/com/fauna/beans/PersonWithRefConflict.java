package com.fauna.beans;


import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;


@ObjectAttribute
public class PersonWithRefConflict {

    @FieldAttribute(name = "@ref")
    public String field = "not";
}