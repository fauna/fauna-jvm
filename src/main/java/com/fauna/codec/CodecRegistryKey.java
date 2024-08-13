package com.fauna.codec;

public class CodecRegistryKey {
    private final Class<?> base;
    private final Class<?> subtype;
    public <T> CodecRegistryKey(Class<?> clazz, Class<?> subtype) {
        base = clazz;
        this.subtype = subtype;
    }

    public static <T> CodecRegistryKey from(Class<?> clazz, Class<?> subtype) {
        return new CodecRegistryKey(clazz, subtype);
    }

    // TODO: Override equals/hash
}
