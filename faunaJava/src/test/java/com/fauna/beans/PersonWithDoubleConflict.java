package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;


@FaunaObject
public class PersonWithDoubleConflict {

    @FaunaField(name = "@double")
    public String field = "not";
}