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
        Query q1 = fql("let one = ${a}", Map.of("a", 0xf));
        String serialized = Serializer.ser(var);
        assertEquals(11 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@int"));
        IDeserializer<Byte> deserializer = Deserializer.generate(ctx, Byte.class);
        assertEquals(var, deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

    @ParameterizedTest
    @ValueSource(shorts = {Short.MIN_VALUE, -128, 0, 127, Short.MAX_VALUE})
    public void testShort(short var) throws IOException {
        Query q1 = fql("let one = ${a}", Map.of("a", 0xf));
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
        Query q1 = fql("let one = ${a}", Map.of("a", 0xf));
        String serialized = Serializer.ser(var);
        // {"@int": ${a}}
        assertEquals(11 + String.valueOf(var).length(), serialized.length());
        assertTrue(serialized.contains("@int"));
        IDeserializer<Integer> deserializer = Deserializer.generate(ctx, Integer.class);
        assertEquals(var, (int) deserializer.deserialize(ctx, new FaunaParser(serialized)));
    }

}
