package com.fauna.serialization;

import com.fauna.exception.SerializationException;
import java.io.IOException;
import java.util.function.Function;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


public class DeserializerTest {

    private static <T> T deserialize(String str,
        Function<SerializationContext, IDeserializer<T>> deserFunc) throws IOException {
        return deserializeImpl(str, deserFunc);
    }

    private static <T> T deserializeImpl(String str,
        Function<SerializationContext, IDeserializer<T>> deserFunc)
        throws IOException {
        FaunaParser reader = new FaunaParser(str);
        reader.read();
        SerializationContext context = new SerializationContext();
        IDeserializer<T> deser = deserFunc.apply(context);
        T obj = deser.deserialize(context, reader);

        if (reader.read()) {
            throw new SerializationException(
                "Token stream is not exhausted but should be: " + reader.getCurrentTokenType());
        }

        return obj;
    }

    @Test
    public void testDeserializeInt() throws IOException {
        int result = deserialize("{\"@int\":\"42\"}",
            ctx -> Deserializer.generate(ctx, Integer.class));
        Assert.assertEquals(42, result);
    }

    @Test
    public void testDeserializeString() throws IOException {
        String result = deserialize("\"hello\"",
            ctx -> Deserializer.generate(ctx, String.class));
        Assert.assertEquals("hello", result);
    }
}