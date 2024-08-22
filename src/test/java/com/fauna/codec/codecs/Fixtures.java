package com.fauna.codec.codecs;


public class Fixtures {

    public static String INT_WIRE(Integer i) {
        return String.format("{\"@int\":\"%s\"}", i);
    }

    public static String LONG_WIRE(Long l) {
        return String.format("{\"@long\":\"%s\"}", l);
    }

    public static String DOUBLE_WIRE(Double d){
        return String.format("{\"@double\":\"%s\"}", d);
    }

    public static String DOUBLE_WIRE(Float f) {
        return String.format("{\"@double\":\"%s\"}", f);
    }

    public static String TIME_WIRE(String s) {
        return String.format("{\"@time\":\"%s\"}", s);
    }
    public static String ESCAPED_OBJECT_WIRE_WITH(String tag) {
        return String.format("{\"@object\":{\"%s\":\"not\"}}", tag);
    }
}
