package com.fauna.beans;

public class Circular {
    public static class Inner {
        public Circular outer;
    }

    public Inner inner;
}
