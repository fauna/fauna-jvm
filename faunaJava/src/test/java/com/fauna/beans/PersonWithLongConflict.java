package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;

@FaunaObject
public class PersonWithLongConflict {

    @FaunaField(name = "@long")
    public String field = "not";
}