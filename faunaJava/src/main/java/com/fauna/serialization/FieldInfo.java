package com.fauna.serialization;

import com.fauna.common.enums.FaunaType;

public class FieldInfo {

    private final String name;
    private final FaunaType type;

    public FieldInfo(String name, FaunaType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public FaunaType getType() {
        return type;
    }
}