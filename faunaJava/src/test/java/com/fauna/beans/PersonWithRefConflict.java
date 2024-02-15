package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;


@FaunaObject
public class PersonWithRefConflict {

    @FaunaField(name = "@ref")
    public String field = "not";
}