package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;


@FaunaObject
public class PersonWithObjectConflict {

    @FaunaField(name = "@object")
    public String field = "not";
}