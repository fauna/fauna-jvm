package com.fauna.beans;


import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;

@ObjectAttribute
public class PersonWithLongConflict {

    @FieldAttribute(name = "@long")
    public String field = "not";
}