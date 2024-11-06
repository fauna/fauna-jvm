package com.fauna.codec;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class CodecRegistryKey {
    private final Class<?> base;
    private final Type[] typeArgs;

    public <T> CodecRegistryKey(Class<T> clazz, Type[] typeArgs) {
        base = clazz;
        this.typeArgs = typeArgs;
    }

    public static <T> CodecRegistryKey from(Class<T> clazz) {
        return new CodecRegistryKey(clazz, null);
    }

    public static <T> CodecRegistryKey from(Class<T> clazz, Type[] typeArgs) {
        return new CodecRegistryKey(clazz, typeArgs);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof CodecRegistryKey) {
            CodecRegistryKey otherCRK = (CodecRegistryKey) other;
            return Objects.equals(base, otherCRK.base) &&
                    Arrays.equals(typeArgs, otherCRK.typeArgs);
        } else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(base, Arrays.hashCode(typeArgs));
    }
}
