package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;

import java.util.Objects;


@FaunaObject
public class ClassWithRefTagCollision {

    @FaunaField(name = "@ref")
    public String field;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        if (getClass() != o.getClass()) return false;

        ClassWithRefTagCollision c = (ClassWithRefTagCollision) o;

        return field.equals(c.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field);
    }
}
