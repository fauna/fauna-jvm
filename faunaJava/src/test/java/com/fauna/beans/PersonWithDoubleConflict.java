package com.fauna.beans;


import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;


@ObjectAttribute
public class PersonWithDoubleConflict {

    @FieldAttribute(name = "@double")
    public String field = "not";
}