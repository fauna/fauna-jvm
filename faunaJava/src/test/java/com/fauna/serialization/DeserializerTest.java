package com.fauna.serialization;

import com.fauna.exception.SerializationException;
import java.io.IOException;
import java.util.function.Function;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


public class DeserializerTest {

    private static <T> T deserialize(String str, Class<T> clazz) throws IOException {
        return deserializeImpl(str, ctx -> Deserializer.generate(ctx, clazz));
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
    public void testDeserializeIntGeneric() throws IOException {
        int result = deserialize("{\"@int\":\"42\"}", Integer.class);
        Assert.assertEquals(42, result);
    }
}