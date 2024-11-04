package com.fauna.beans;


import com.fauna.annotation.FaunaField;

import java.util.Objects;


public class ClassWithRefTagCollision {

    public ClassWithRefTagCollision() {
    }

    public ClassWithRefTagCollision(String field) {
        this.field = field;
    }

    @FaunaField(name = "@ref")
    public String field;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null) {
        return false;
      }

      if (getClass() != o.getClass()) {
        return false;
      }

        ClassWithRefTagCollision c = (ClassWithRefTagCollision) o;

        return field.equals(c.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field);
    }
}
