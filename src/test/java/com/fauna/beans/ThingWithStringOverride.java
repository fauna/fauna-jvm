package com.fauna.beans;

public class ThingWithStringOverride {

    private static final String Name = "TheThing";

    @Override
    public String toString() {
        return Name;
    }
}