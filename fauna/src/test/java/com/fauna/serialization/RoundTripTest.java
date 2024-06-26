package com.fauna.serialization;

import com.fauna.interfaces.IDeserializer;
import com.fauna.mapping.MappingContext;
import com.fauna.query.builder.Query;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Map;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoundTripTest {

    MappingContext ctx = new MappingContext();

    @ParameterizedTest
    @ValueSource(bytes = {-128, 0, 127})
    public void testBytes(byte var) throws IOException {
        String serialized = Serializer.ser(var);
        assertEquals(11 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@int"));
        IDeserializer<Byte> deserializer = Deserializer.generate(ctx, Byte.class);
        assertEquals(var, deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(shorts = {Short.MIN_VALUE, -128, 0, 127, Short.MAX_VALUE})
    public void testShort(short var) throws IOException {
        String serialized = Serializer.ser(var);
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
        String serialized = Serializer.ser(var);
        // {"@int": ${a}}
        assertEquals(11 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@int"));
        IDeserializer<Integer> deserializer = Deserializer.generate(ctx, Integer.class);
        assertEquals(var, (int) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MAX_VALUE, -128, 0, 127, Long.MAX_VALUE})
    public void testLong(long var) throws IOException {
        String serialized = Serializer.ser(var);
        // {"@long": ${a}}
        assertEquals(12 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@long"));
        IDeserializer<Long> deserializer = Deserializer.generate(ctx, Long.class);
        assertEquals(var, (long) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(floats = {Float.MIN_VALUE, -128f, 0.1e-10f, 0.0f, 0.1e10f, 127.0f, Float.MAX_VALUE})
    public void testFloat(float var) throws IOException {
        String serialized = Serializer.ser(var);
        // assertEquals(15 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@double"));
        IDeserializer<Float> deserializer = Deserializer.generate(ctx, Float.class);
        assertEquals(var, (float) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.MIN_VALUE, -128, 0.1e-10, 0.0, 0.1e10, 127.0f, Double.MAX_VALUE})
    public void testDouble(double var) throws IOException {
        String serialized = Serializer.ser(var);
        // assertEquals(24 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@double"));
        IDeserializer<Double> deserializer = Deserializer.generate(ctx, Double.class);
        assertEquals(var, (double) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(chars = {'a', 'b', 0x2202})
    public void testChar(char var) throws IOException {
        String serialized = Serializer.ser(var);
        assertEquals(3, serialized.length());
        assertTrue(serialized.contains(String.valueOf(var)));
        IDeserializer<Character> deserializer = Deserializer.generate(ctx, Character.class);
        assertEquals(var, (char) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }


}
