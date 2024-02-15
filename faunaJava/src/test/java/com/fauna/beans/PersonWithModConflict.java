package com.fauna.beans;


import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;


@ObjectAttribute
public class PersonWithModConflict {

    @FieldAttribute(name = "@mod")
    public String field = "not";
}