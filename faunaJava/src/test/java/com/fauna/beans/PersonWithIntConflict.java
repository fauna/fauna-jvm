package com.fauna.beans;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;

@FaunaObject
public class PersonWithIntConflict {

    @FaunaField(name = "@int")
    public String field = "not";
}