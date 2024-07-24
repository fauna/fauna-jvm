package com.fauna.helper;

import com.fauna.types.Page;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class PageOf implements ParameterizedType {
    /*
        TODO: This class is only used in the tests?
     */

    private final Class clazz;

    public PageOf(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{clazz};
    }

    @Override
    public Type getRawType() {
        return Page.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}