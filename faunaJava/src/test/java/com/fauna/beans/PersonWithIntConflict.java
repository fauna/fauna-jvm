package com.fauna.beans;

import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;

@ObjectAttribute
public class PersonWithIntConflict {

    @FieldAttribute(name = "@int")
    public String field = "not";
}