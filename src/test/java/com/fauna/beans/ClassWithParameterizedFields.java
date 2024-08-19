package com.fauna.beans;

import com.fauna.annotation.FaunaField;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClassWithParameterizedFields {

    public ClassWithParameterizedFields() {
    }

    public ClassWithParameterizedFields(String firstName, List<String> list, Map<String,Integer> map) {
        this.firstName = firstName;
        this.list = list;
        this.map = map;
    }

    @FaunaField(name = "first_name")
    public String firstName;

    @FaunaField(name = "a_list", typeArgument = String.class)
    public List<String> list;

    @FaunaField(name = "a_map", typeArgument = Integer.class)
    public Map<String,Integer> map;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        ClassWithParameterizedFields c = (ClassWithParameterizedFields) o;

        return list.equals(c.list)
                && map.equals(c.map)
                && firstName.equals(c.firstName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, map, firstName);
    }
}