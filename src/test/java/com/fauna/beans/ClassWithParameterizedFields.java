package com.fauna.beans;

import com.fauna.annotation.FaunaField;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ClassWithParameterizedFields {

    public ClassWithParameterizedFields() {
    }

    public ClassWithParameterizedFields(String firstName, List<ClassWithAttributes> list, Map<String,Integer> map, Optional<String> optional) {
        this.firstName = firstName;
        this.list = list;
        this.map = map;
        this.optional = optional;
    }

    @FaunaField(name = "first_name")
    public String firstName;

    @FaunaField(name = "a_list")
    public List<ClassWithAttributes> list;

    @FaunaField(name = "a_map")
    public Map<String,Integer> map;

    @FaunaField(name = "an_optional")
    public Optional<String> optional;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        ClassWithParameterizedFields c = (ClassWithParameterizedFields) o;

        return list.equals(c.list)
                && map.equals(c.map)
                && Objects.equals(optional, c.optional)
                && firstName.equals(c.firstName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, map, firstName, optional);
    }
}