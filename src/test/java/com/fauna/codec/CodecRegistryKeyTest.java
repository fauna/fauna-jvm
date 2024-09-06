package com.fauna.codec;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CodecRegistryKeyTest {


    @Test
    public void equals_classAndSubClassNotNullAreEqual() {
        CodecRegistryKey key1 = CodecRegistryKey.from(String.class, new Type[]{Integer.class});
        CodecRegistryKey key2 = CodecRegistryKey.from(String.class, new Type[]{Integer.class});
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void equals_classAndSubclassNullAreEqual() {
        CodecRegistryKey key1 = CodecRegistryKey.from(null, null);
        CodecRegistryKey key2 = CodecRegistryKey.from(null, null);
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void equals_differentClassesNotEqual() {
        CodecRegistryKey key1 = CodecRegistryKey.from(String.class, new Type[]{Integer.class});
        CodecRegistryKey key2 = CodecRegistryKey.from(Object.class, new Type[]{Integer.class});
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void equals_differentSubClassesNotEqual() {
        CodecRegistryKey key1 = CodecRegistryKey.from(String.class, new Type[]{Integer.class});
        CodecRegistryKey key2 = CodecRegistryKey.from(String.class, new Type[]{Object.class});
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }
}
