package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;


@FaunaObject
public class PersonWithSetConflict {

    @FaunaField(name = "@set")
    public String field = "not";
}