package com.fauna.stream;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.event.StreamRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamRequestTest {
    public static final CodecProvider provider = DefaultCodecProvider.SINGLETON;

    @Test
    public void testTokenOnlyRequest() {
        StreamRequest req = StreamRequest.builder("abc").build();
        assertEquals("abc", req.getToken());
        assertTrue(req.getCursor().isEmpty());
        assertTrue(req.getStartTs().isEmpty());
    }

    @Test
    public void testCursorRequest() {
        StreamRequest req = StreamRequest.builder("abc").cursor("def").build();
        assertEquals("abc", req.getToken());
        assertEquals("def", req.getCursor().get());
        assertTrue(req.getStartTs().isEmpty());
    }

    @Test
    public void testTsRequest() {
        StreamRequest req = StreamRequest.builder("abc").startTs(1234L).build();
        assertEquals("abc", req.getToken());
        assertTrue(req.getCursor().isEmpty());
        assertEquals(1234L, req.getStartTs().get());
    }

    @Test
    public void testCursorAndTsRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> StreamRequest.builder("tkn").startTs(10L)
                        .cursor("hello"));
        assertThrows(IllegalArgumentException.class,
                () -> StreamRequest.builder("tkn").cursor("hello")
                        .startTs(10L));
    }

}
