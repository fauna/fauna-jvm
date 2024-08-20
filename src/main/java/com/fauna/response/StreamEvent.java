package com.fauna.response;

import com.fauna.response.wire.StreamEventWire;

public class StreamEvent {
    private final StreamEventWire wire;

    public StreamEvent(StreamEventWire wire) {
        this.wire = wire;
        if (wire.getData() != null) {
        }
    }


}
