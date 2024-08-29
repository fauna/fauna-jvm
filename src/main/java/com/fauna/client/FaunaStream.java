package com.fauna.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.ClientException;
import com.fauna.response.StreamEvent;
import com.fauna.response.wire.StreamEventWire;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.Collectors;


public class FaunaStream<E> implements Processor<List<ByteBuffer>, StreamEvent<E>> {
    ObjectMapper mapper = new ObjectMapper();
    private final Class elementClass;
    private final Codec<StreamEventWire> eventCodec;
    private Subscription subscription;
    private Thread processor;
    private Subscriber<? super StreamEvent<E>> eventSubscriber;

    public FaunaStream(CompletableFuture<HttpResponse<Publisher<List<ByteBuffer>>>> streamResponse, Class<E> elementClass) {
        this.elementClass = elementClass;
        this.eventCodec = DefaultCodecProvider.SINGLETON.get(StreamEventWire.class);
        streamResponse.thenAccept(response -> {response.body().subscribe(this);});
    }

    @Override
    public void subscribe(Subscriber<? super StreamEvent<E>> subscriber) {
        if (this.eventSubscriber == null) {
            this.eventSubscriber = subscriber;
        } else {
            throw new ClientException("Only one subscriber is supported.");
        }

    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.processor = new Thread(() -> {
            while (true) {
                try {
                    this.subscription.request(1);
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // handle: log or throw in a wrapped RuntimeException
                    throw new RuntimeException("InterruptedException caught in lambda", e);
                }

            }
        });
        this.processor.start();
    }

    @Override
    public void onNext(List<ByteBuffer> buffers) {
        String timestamp = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
        System.out.println(MessageFormat.format("{0} -> processing {1} buffers " , timestamp, buffers.size()));
        try {
            // TODO: Use a codec for StreamEventWire (or possibly decode straight to StreamEvent).
            // I think this will also allow us to handle the case where onNext gets called with multiple events in
            // the buffers, or if the buffers have an incomplete event (e.g. with a large document).
            // StreamEventWire wire = eventCodec.decode(new UTF8FaunaParser(buffers));
            String bufs = buffers.stream()
                    .map(b -> StandardCharsets.UTF_8.decode(b).toString())
                    .collect(Collectors.joining());
            StreamEventWire wire = mapper.readValue(bufs, StreamEventWire.class);
            this.eventSubscriber.onNext(new StreamEvent<E>(wire, elementClass));
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        this.processor.interrupt();
        System.out.println("FaunaStream onComplete: ");

    }
}
