package com.fauna.beans;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;


@FaunaObject
public class PersonWithDocConflict {

    @FaunaField(name = "@doc")
    public String field = "not";
}