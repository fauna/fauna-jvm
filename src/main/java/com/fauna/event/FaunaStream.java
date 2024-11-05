package com.fauna.event;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fauna.client.StatsCollector;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.ClientException;
import com.fauna.response.ErrorInfo;
import com.fauna.response.MultiByteBufferInputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;


public class FaunaStream<E> extends SubmissionPublisher<FaunaEvent<E>> implements Processor<List<ByteBuffer>, FaunaEvent<E>> {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private final Codec<E> dataCodec;
    private Subscription subscription;
    private Subscriber<? super FaunaEvent<E>> eventSubscriber;
    private MultiByteBufferInputStream buffer = null;
    private final StatsCollector statsCollector;

    public FaunaStream(Class<E> elementClass, StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        this.dataCodec = DefaultCodecProvider.SINGLETON.get(elementClass);
    }

    @Override
    public void subscribe(Subscriber<? super FaunaEvent<E>> subscriber) {
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
                    FaunaEvent<E> event = FaunaEvent.parse(parser, dataCodec);

                    statsCollector.add(event.getStats());

                    if (event.getType() == FaunaEvent.EventType.ERROR) {
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