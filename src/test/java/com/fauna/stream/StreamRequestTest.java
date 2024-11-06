package com.fauna.stream;

import com.fauna.codec.CodecProvider;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.event.EventSource;
import com.fauna.event.StreamOptions;
import com.fauna.event.StreamRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StreamRequestTest {
    public static final CodecProvider provider = DefaultCodecProvider.SINGLETON;
    private final static EventSource SOURCE = new EventSource("abc");

    @Test
    public void testTokenOnlyRequest() throws IOException {
        StreamRequest req = new StreamRequest(SOURCE, StreamOptions.DEFAULT);
        assertEquals("{\"token\":\"abc\"}", req.serialize());
    }

    @Test
    public void testCursorRequest() throws IOException {
        StreamRequest req = new StreamRequest(SOURCE, StreamOptions.builder().cursor("def").build());
        assertEquals("{\"token\":\"abc\",\"cursor\":\"def\"}", req.serialize());
    }

    @Test
    public void testTsRequest() throws IOException {
        StreamRequest req = new StreamRequest(SOURCE, StreamOptions.builder().startTimestamp(1234L).build());
        assertEquals("{\"token\":\"abc\",\"start_ts\":1234}", req.serialize());
    }

    @Test
    public void testMissingArgsRequest() {
        assertThrows(IllegalArgumentException.class, () -> new StreamRequest(SOURCE, null));
        assertThrows(IllegalArgumentException.class, () -> new StreamRequest(null, StreamOptions.DEFAULT));
    }

}
