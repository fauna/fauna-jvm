package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.Objects;

public class CodecRegistryKey {
    private final Class<?> base;
    private final Type typeArg;
    public <T> CodecRegistryKey(Class<T> clazz, Type typeArg) {
        base = clazz;
        this.typeArg = typeArg;
    }

    public static <T> CodecRegistryKey from(Class<T> clazz) {
        return new CodecRegistryKey(clazz, null);
    }

    public static <T> CodecRegistryKey from(Class<T> clazz, Type typeArg) {
        return new CodecRegistryKey(clazz, typeArg);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof CodecRegistryKey))
            return false;
        CodecRegistryKey other = (CodecRegistryKey)o;

        return base == other.base && typeArg == other.typeArg;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(base, typeArg);
    }
}
