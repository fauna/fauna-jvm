package com.fauna.codec;

import com.fauna.codec.codecs.IntCodec;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CodecRegistryTest {

    CodecRegistry reg = new DefaultCodecRegistry();

    @Test
    public void put_addsCodecWithKey() {
        CodecRegistryKey key = CodecRegistryKey.from(String.class, new Type[]{Integer.class});
        Codec<Integer> codec = new IntCodec();
        reg.put(key, codec);
        Codec result = reg.get(key);
        assertEquals(codec, result);
    }
}
