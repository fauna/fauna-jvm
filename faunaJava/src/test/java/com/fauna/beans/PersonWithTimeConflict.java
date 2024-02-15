package com.fauna.beans;


import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;

@ObjectAttribute
public class PersonWithTimeConflict {

    @FieldAttribute(name = "@time")
    public String field = "not";
}