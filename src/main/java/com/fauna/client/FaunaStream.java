package com.fauna.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.exception.ClientException;
import com.fauna.response.StreamEvent;
import com.fauna.response.wire.StreamEventWire;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;


public class FaunaStream<E> extends SubmissionPublisher<StreamEvent<E>> implements Processor<List<ByteBuffer>, StreamEvent<E>> {
    ObjectMapper mapper = new ObjectMapper();
    private final Class elementClass;
    private Subscription subscription;
    private Subscriber<? super StreamEvent<E>> eventSubscriber;

    public FaunaStream(Class<E> elementClass) {
        this.elementClass = elementClass;
    }

    @Override
    public void subscribe(Subscriber<? super StreamEvent<E>> subscriber) {
        if (this.eventSubscriber == null) {
            this.eventSubscriber = subscriber;
            super.subscribe(subscriber);
            this.subscription.request(1);
        } else {
            throw new ClientException("Only one subscriber is supported.");
        }

    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public void onNext(List<ByteBuffer> buffers) {
        try {
            // TODO: Use a codec for StreamEventWire (or possibly decode straight to StreamEvent).
            // I think this will also allow us to handle the case where onNext gets called with multiple events in
            // the buffers, or if the buffers have an incomplete event (e.g. with a large document).
            // StreamEventWire wire = eventCodec.decode(new UTF8FaunaParser(buffers));
            String bufs = buffers.stream()
                    .map(b -> StandardCharsets.UTF_8.decode(b).toString())
                    .collect(Collectors.joining());
            StreamEventWire wire = mapper.readValue(bufs, StreamEventWire.class);
            this.submit(new StreamEvent<E>(wire, elementClass));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        this.subscription.cancel();
        System.err.println("FaunaStream onError: " + throwable.getMessage());

    }

    @Override
    public void onComplete() {
        this.subscription.cancel();
        System.out.println("FaunaStream onComplete.");

    }
}
