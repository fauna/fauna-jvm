package com.fauna.client;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.ClientException;
import com.fauna.response.ErrorInfo;
import com.fauna.response.StreamEvent;
import com.fauna.response.MultiByteBufferInputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;


public class FaunaStream<E> extends SubmissionPublisher<StreamEvent<E>> implements Processor<List<ByteBuffer>, StreamEvent<E>> {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
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
                    JsonParser parser = JSON_FACTORY.createParser(buffer);
                    StreamEvent<E> event = StreamEvent.parse(parser, dataCodec);
                    if (event.getType() == StreamEvent.EventType.ERROR) {
                        ErrorInfo error = event.getError();
                        this.onComplete();
                        this.close();
                        throw new ClientException(MessageFormat.format("Stream stopped due to error {0} {1}", error.getCode(), error.getMessage()));
                    }
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
            throw new ClientException("Unable to decode stream", e);
        } finally {
            this.subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        this.subscription.cancel();
        this.close();
    }

    @Override
    public void onComplete() {
        this.subscription.cancel();
    }
}
