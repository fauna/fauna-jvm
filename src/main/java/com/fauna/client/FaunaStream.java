package com.fauna.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.ClientException;
import com.fauna.response.StreamEvent;
import com.fauna.response.wire.MultiByteBufferInputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;


public class FaunaStream<E> extends SubmissionPublisher<StreamEvent<E>> implements Processor<List<ByteBuffer>, StreamEvent<E>> {
    ObjectMapper mapper = new ObjectMapper();
    private final Codec<E> dataCodec;
    private Subscription subscription;
    private Subscriber<? super StreamEvent<E>> eventSubscriber;
    private MultiByteBufferInputStream buffer = null;

    public FaunaStream(Class<E> elementClass) {
        this.dataCodec = DefaultCodecProvider.SINGLETON.get(elementClass);
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
            // Using synchronized is probably not the fastest way to do this.
            synchronized (this) {
                if (this.buffer == null) {
                    this.buffer = new MultiByteBufferInputStream(buffers);
                } else {
                    this.buffer.add(buffers);
                }
                try {
                    JsonParser parser = mapper.getFactory().createParser(buffer);
                    StreamEvent<E> event = StreamEvent.parse(parser, dataCodec);
                    this.submit(event);
                    this.buffer = null;
                } catch (ClientException e) {
                    // Maybe we got a partial event...
                    this.buffer.reset();
                } catch (IOException e) {
                    throw new ClientException("Unable to decode stream", e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
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
