package com.fauna.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.exception.ClientException;
import com.fauna.response.wire.StreamEventWire;
import com.fauna.types.Document;

import java.io.IOException;
import java.util.Optional;

public class StreamEvent<E> {
    private final StreamEventWire wire;
    private final Class<E> elementClass;
    private final E data;

    public StreamEvent(StreamEventWire wire, Class<E> elementClass) {
        this.wire = wire;
        this.elementClass = elementClass;
        try {
            Codec<E> elementCodec = DefaultCodecProvider.SINGLETON.get(elementClass);
            if (wire.getData() != null && wire.getData() != "null") {
                this.data = elementCodec.decode(new UTF8FaunaParser(wire.getData()));
            } else {
                this.data = null;
            }
        } catch (IOException e) {
            throw new ClientException("Failed to parse data.", e);
        }
    }

    public Optional<E> getData() {
        return Optional.ofNullable(data);
    }

    public Long getTimestamp() {
        return this.wire.getTxnTs();
    }

    public String getCursor() {
        return this.wire.getCursor();
    }


}
