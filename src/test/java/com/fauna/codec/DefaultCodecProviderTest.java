package com.fauna.codec;

import com.fauna.beans.Circular;
import com.fauna.codec.codecs.ListCodec;
import com.fauna.codec.codecs.MapCodec;
import com.fauna.types.Document;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultCodecProviderTest {

    CodecRegistry cr = new DefaultCodecRegistry();
    CodecProvider cp = new DefaultCodecProvider(cr);

    @Test
    public void get_returnsRegisteredCodec() {
        Codec<Integer> codec = cp.get(Integer.class, null);
        assertNotNull(codec);
        assertEquals(Integer.class ,codec.getCodecClass());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void get_generatesListCodec() {
        Codec<List<Integer>> codec = (Codec<List<Integer>>) (Codec) cp.get(List.class, new Type[]{Integer.class});
        assertNotNull(codec);
        assertEquals(ListCodec.class, codec.getClass());
        assertEquals(Integer.class, codec.getCodecClass());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void get_generatesMapCodecForImmutableMap() {
        var map = Map.of();
        Codec<Map<String,Integer>> codec = (Codec<Map<String,Integer>>) (Codec) cp.get(map.getClass(), new Type[]{String.class, Integer.class});
        assertNotNull(codec);
        assertEquals(MapCodec.class, codec.getClass());
        assertEquals(Integer.class, codec.getCodecClass());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void get_generatesListCodecForImmutableList() {
        var list = List.of();
        Codec<List<Integer>> codec = (Codec<List<Integer>>) (Codec) cp.get(list.getClass(), new Type[]{Integer.class});
        assertNotNull(codec);
        assertEquals(ListCodec.class, codec.getClass());
        assertEquals(Integer.class, codec.getCodecClass());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void get_generateForClassWithCircularRef() {
        Codec<Circular> codec = cp.get(Circular.class, null);
        assertNotNull(codec);
        assertEquals(Circular.class, codec.getCodecClass());
    }
}
