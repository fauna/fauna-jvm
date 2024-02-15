package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;


@FaunaObject
public class PersonWithModConflict {

    @FaunaField(name = "@mod")
    public String field = "not";
}