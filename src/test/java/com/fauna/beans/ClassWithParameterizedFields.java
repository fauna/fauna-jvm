package com.fauna.beans;

import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;

import java.util.List;
import java.util.Map;

@FaunaObject
public class ClassWithParameterizedFields {

    @FaunaField(name = "first_name")
    public String firstName = "Baz";

    @FaunaField(name = "a_list", typeArgument = String.class)
    public List<String> list  = List.of("item1");

    @FaunaField(name = "a_map", typeArgument = Integer.class)
    public Map<String,Integer> map  = Map.of("key1", 42);
}