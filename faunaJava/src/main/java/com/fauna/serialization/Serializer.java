package com.fauna.serialization;

import java.util.HashSet;
import java.util.Set;

public class Serializer {

    public static final Set<String> TAGS = new HashSet<>(Set.of(
        "@int", "@long", "@double", "@date", "@time", "@mod", "@ref", "@doc", "@set", "@object"
    ));
}
