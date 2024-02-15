package com.fauna.beans;

import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;


@ObjectAttribute
public class PersonWithDocConflict {

    @FieldAttribute(name = "@doc")
    public String field = "not";
}