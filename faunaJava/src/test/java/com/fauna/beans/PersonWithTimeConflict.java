package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;

@FaunaObject
public class PersonWithTimeConflict {

    @FaunaField(name = "@time")
    public String field = "not";
}