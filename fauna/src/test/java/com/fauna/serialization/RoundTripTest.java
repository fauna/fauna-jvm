package com.fauna.serialization;

import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoundTripTest {

    MappingContext ctx = new MappingContext();

    @ParameterizedTest
    @ValueSource(bytes = {-128, 0, 127})
    public void testBytes(byte var) throws IOException {
        String serialized = Serializer.serialize(var);
        assertEquals(11 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@int"));
        IDeserializer<Byte> deserializer = Deserializer.generate(ctx, Byte.class);
        assertEquals(var, deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(shorts = {Short.MIN_VALUE, -128, 0, 127, Short.MAX_VALUE})
    public void testShort(short var) throws IOException {
        String serialized = Serializer.serialize(var);
        // {"@int": ${a}}
        assertEquals(11 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@int"));
        IDeserializer<Short> deserializer = Deserializer.generate(ctx, Short.class);
        int deserialized = deserializer.deserialize(ctx, new FaunaParser(serialized));
        assertEquals(var, deserialized);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -128, 0, 127, Integer.MAX_VALUE})
    public void testInteger(int var) throws IOException {
        String serialized = Serializer.serialize(var);
        // {"@int": ${a}}
        assertEquals(11 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@int"));
        IDeserializer<Integer> deserializer = Deserializer.generate(ctx, Integer.class);
        assertEquals(var, (int) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MAX_VALUE, -128, 0, 127, Long.MAX_VALUE})
    public void testLong(long var) throws IOException {
        String serialized = Serializer.serialize(var);
        // {"@long": ${a}}
        assertEquals(12 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@long"));
        IDeserializer<Long> deserializer = Deserializer.generate(ctx, Long.class);
        assertEquals(var, (long) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(floats = {Float.MIN_VALUE, -128f, 0.1e-10f, 0.0f, 0.1e10f, 127.0f, Float.MAX_VALUE})
    public void testFloat(float var) throws IOException {
        String serialized = Serializer.serialize(var);
        // assertEquals(15 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@double"));
        IDeserializer<Float> deserializer = Deserializer.generate(ctx, Float.class);
        assertEquals(var, (float) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.MIN_VALUE, -128, 0.1e-10, 0.0, 0.1e10, 127.0f, Double.MAX_VALUE})
    public void testDouble(double var) throws IOException {
        String serialized = Serializer.serialize(var);
        // assertEquals(24 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@double"));
        IDeserializer<Double> deserializer = Deserializer.generate(ctx, Double.class);
        assertEquals(var, (double) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(chars = {'a', 'b', 'k', 0x2202})
    public void testChar(char var) throws IOException {
        String serialized = Serializer.serialize(var);
        assertTrue(serialized.contains("@int"));
        IDeserializer<Character> deserializer = Deserializer.generate(ctx, Character.class);
        char deser = (char) deserializer.deserialize(ctx, new FaunaParser(serialized));
        assertEquals(var, deser);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testBoolean(boolean var) throws IOException {
        String serialized = Serializer.serialize(var);
        assertEquals(String.valueOf(var), serialized);
        IDeserializer<Boolean> deserializer = Deserializer.generate(ctx, Boolean.class);
        assertEquals(var, deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @Test
    public void testMap() throws IOException {
        Map<String, Object> var = Map.of("foo", 1, "bar", "two");
        String serialized = Serializer.serialize(var);
        assertTrue(serialized.contains("{\"@int\":\"1\"}"));
        //assertEquals("{\"foo\":{\"@int\":\"1\"},\"bar\":\"two\"}", serialized);

        assertEquals(var, Deserializer.DYNAMIC.deserialize(ctx, new FaunaParser(serialized)));
    }

    @Test
    public void testList() throws IOException {
        List<Object> var = List.of("foo", 1, "bar", "two");
        String serialized = Serializer.serialize(var);
        assertTrue(serialized.contains("{\"@int\":\"1\"}"));
        assertEquals("[\"foo\",{\"@int\":\"1\"},\"bar\",\"two\"]", serialized);
        IDeserializer deserializer = Deserializer.DYNAMIC;
        assertEquals(var, deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }



    // @Disabled("Byte array deserialization not supported yet.")
    @Test
    public void testByteArray() throws IOException {
        byte[] var = new byte[]{-128, 0, 127};
        String serialized = Serializer.serialize(var);
        assertEquals("{\"@bytes\":\"gAB/\"}", serialized);
        // IDeserializer<Byte[]> deserializer = Deserializer.generate(ctx, Byte[].class);
        Object actual = Deserializer.DYNAMIC.deserialize(ctx, new FaunaParser(serialized));
        // TODO: Should get a byte[] or Byte[] not {"@bytes": "gAB\"} back.
        assertEquals(Map.of("@bytes", "gAB/"), actual);
    }

    @Disabled("Object array deserialization not supported yet.")
    @Test
    public void testObjectArray() throws IOException {
        Object[] var = new Object[]{-127, 12.5, "hello"};
        String serialized = Serializer.serialize(var);
        // assertEquals("{\"@shorts\":\"gAB/\"}", serialized);
        IDeserializer<Object[]> deserializer = Deserializer.generate(ctx, Object[].class);
        assertEquals(var, deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @Disabled("Primitive arrays not supported yet")
    @Test
    public void testShortArray() throws IOException {
        short[] var = new short[]{-128, 0, 127};
        String serialized = Serializer.serialize(var);
        assertEquals("{\"@shorts\":\"gAB/\"}", serialized);
        IDeserializer<Byte[]> deserializer = Deserializer.generate(ctx, short[].class);
        assertEquals(var, deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @Disabled("Primitive arrays not supported yet")
    @Test
    public void testIntArray() throws IOException {
        int[] var = new int[]{-128, 0, 127};
        String serialized = Serializer.serialize(var);
        assertEquals("{\"@ints\":\"gAB/\"}", serialized);
        IDeserializer<Integer[]> deserializer = Deserializer.generate(ctx, int[].class);
        assertEquals(var, deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

}
