package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;

@FaunaObject
public class PersonWithDateConflict {

    @FaunaField(name = "@date")
    public String field = "not";
}
