package com.fauna.stream;

import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.UTF8FaunaGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamRequestTest {
    public static final CodecProvider provider = DefaultCodecProvider.SINGLETON;

    @Test
    public void testTokenOnlyRequest() {
        StreamRequest req = new StreamRequest("abc");
        assertEquals("abc", req.getToken());
        assertTrue(req.getCursor().isEmpty());
        assertTrue(req.getStartTs().isEmpty());
    }

    @Test
    public void testCursorRequest() {
        StreamRequest req = new StreamRequest("abc", "def");
        assertEquals("abc", req.getToken());
        assertEquals("def", req.getCursor().get());
        assertTrue(req.getStartTs().isEmpty());
    }

    @Test
    public void testTsRequest() {
        StreamRequest req = new StreamRequest("abc", 1234L);
        assertEquals("abc", req.getToken());
        assertTrue(req.getCursor().isEmpty());
        assertEquals(1234L, req.getStartTs().get());
    }

    @Test
    public void testSerialization() throws IOException {
        UTF8FaunaGenerator generator = new UTF8FaunaGenerator();
        Codec<StreamRequest> codec = provider.get(StreamRequest.class);
        StreamRequest req = new StreamRequest("abc");
        codec.encode(generator, req);
        // End object '}' has to be manually added in RequestBuilder.java
        assertEquals("{\"token\":\"abc\"", generator.serialize());
    }

    @Test
    public void testSerializationWithCursor() throws IOException {
        UTF8FaunaGenerator generator = new UTF8FaunaGenerator();
        Codec<StreamRequest> codec = provider.get(StreamRequest.class);
        StreamRequest req = new StreamRequest("abc", "def");
        codec.encode(generator, req);
        // End object '}' has to be manually added in RequestBuilder.java
        assertEquals("{\"token\":\"abc\",\"cursor\":\"def\"", generator.serialize());
    }

}
