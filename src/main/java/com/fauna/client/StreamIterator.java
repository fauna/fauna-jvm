package com.fauna.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.async.ByteArrayFeeder;
import com.fasterxml.jackson.core.async.ByteBufferFeeder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.exception.ClientException;
import com.fauna.query.StreamOptions;
import com.fauna.response.StreamEvent;
import com.fauna.response.wire.StreamEventWire;
import com.fauna.stream.StreamRequest;
import com.fauna.query.StreamTokenResponse;
import com.fauna.response.QuerySuccess;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class StreamIterator implements Iterator<StreamEvent> {
    ObjectMapper mapper = new ObjectMapper();
    FaunaClient client;
    String token;
    Class resultClass;
    InputStream stream;
    StreamOptions options;
    JsonParser parser;
    CompletableFuture<QuerySuccess<StreamTokenResponse>> tokenFuture;

    public StreamIterator(FaunaClient client, CompletableFuture<QuerySuccess<StreamTokenResponse>> tokenFuture, Class resultClass, StreamOptions options) {
        this.client = client;
        this.tokenFuture = tokenFuture;
        this.resultClass = resultClass;
        this.options = options;
        this.stream = openStream();
        this.parser = parse();
    }

    private InputStream openStream() {
        try {
            QuerySuccess<StreamTokenResponse> success = tokenFuture.get();
            String token = success.getData().getToken();
            return client.openStream(new StreamRequest(token)).get().body();
        } catch (InterruptedException | ExecutionException e) {
            throw new ClientException("Unable to open stream.");
        }
    }

    private JsonParser parse() {
        try {
            // Can also call createNonBlockingByteBufferParser, but I could not get that to work.
            JsonParser parser = mapper.getFactory().createNonBlockingByteArrayParser();
            ByteArrayFeeder feeder = (ByteArrayFeeder) parser.getNonBlockingInputFeeder();
            if (this.stream.available() > 0) {
                byte[] bytes = new byte[1024]; // Max Document size is 8MB.
                int j = this.stream.read(bytes, 0, 1024);
                feeder.feedInput(bytes, 0, j);
                return parser;
            } else {
                return null;
            }
        } catch (JsonParseException e) {
            throw new ClientException("Unable to parse stream as JSON.");
        } catch (IOException e) {
            throw new ClientException("Exception handling stream.");
        }
    }

    public void tryParse() {
        if (this.parser == null) {
            this.parser = parse();
        }
    }


    @Override
    public boolean hasNext() {
        tryParse();
        // This might break the iterator contract that hasNext can flip from false -> true.
        // In that case, we could rename this "FaunaStream" or something like that.
        return this.parser != null;
    }

    @Override
    public StreamEvent next() {
        tryParse();
        if (this.parser == null) {
            throw new NoSuchElementException();
        }
        try {
            // TODO: Actually parse the event data, and don't return status events depending on StreamOptions.
            StreamEvent event = new StreamEvent(mapper.readValue(parser, StreamEventWire.class));
            this.parser = null;
            return event;
        } catch (IOException e) {
            throw new NoSuchElementException();
        }
    }
}
