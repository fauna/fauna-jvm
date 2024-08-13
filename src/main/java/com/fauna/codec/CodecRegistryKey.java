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
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof CodecRegistryKey) {
            CodecRegistryKey otherCRK = (CodecRegistryKey) other;
            return this.base == otherCRK.base && this.typeArg == otherCRK.typeArg;
        } else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(base, typeArg);
    }
}
