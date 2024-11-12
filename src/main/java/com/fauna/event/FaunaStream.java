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

/**
 * A processor for handling and decoding Fauna <a href="https://docs.fauna.com/fauna/current/reference/cdc/#event-streaming">Event Streams</a>.
 * <p>
 * The {@code FaunaStream} class extends {@link SubmissionPublisher} to process
 * incoming ByteBuffers, decode them into {@link FaunaEvent} objects, and forward
 * them to subscribers.
 *
 * @param <E> The type of document data contained in the Fauna events.
 */
public class FaunaStream<E> extends SubmissionPublisher<FaunaEvent<E>>
        implements Processor<List<ByteBuffer>, FaunaEvent<E>> {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private final Codec<E> dataCodec;
    private Subscription subscription;
    private Subscriber<? super FaunaEvent<E>> eventSubscriber;
    private MultiByteBufferInputStream buffer = null;
    private final StatsCollector statsCollector;

    /**
     * Constructs a {@code FaunaStream} instance with the specified event data type and stats collector.
     *
     * @param elementClass   The class of the event data type.
     * @param statsCollector The {@link StatsCollector} to track statistics for events.
     */
    public FaunaStream(final Class<E> elementClass, final StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
        this.dataCodec = DefaultCodecProvider.SINGLETON.get(elementClass);
    }

    /**
     * Subscribes a single subscriber to this stream.
     *
     * @param subscriber The {@link Subscriber} to subscribe to this stream.
     * @throws ClientException if more than one subscriber attempts to subscribe.
     */
    @Override
    public void subscribe(final Subscriber<? super FaunaEvent<E>> subscriber) {
        if (this.eventSubscriber == null) {
            this.eventSubscriber = subscriber;
            super.subscribe(subscriber);
            this.subscription.request(1);
        } else {
            throw new ClientException("Only one subscriber is supported.");
        }
    }

    /**
     * Handles subscription by setting the subscription and requesting data.
     *
     * @param subscription The subscription to this stream.
     */
    @Override
    public void onSubscribe(final Subscription subscription) {
        this.subscription = subscription;
    }

    /**
     * Processes incoming ByteBuffers, decodes them into Fauna events, and submits the events to subscribers.
     *
     * @param buffers The list of {@link ByteBuffer}s containing encoded event data.
     * @throws ClientException if there is an error decoding the stream or processing events.
     */
    @Override
    public void onNext(final List<ByteBuffer> buffers) {
        try {
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
                        throw new ClientException(MessageFormat.format(
                                "Stream stopped due to error {0} {1}",
                                error.getCode(), error.getMessage()));
                    }
                    this.submit(event);
                    this.buffer = null;
                } catch (ClientException e) {
                    this.buffer.reset(); // Handles partial event decoding
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

    /**
     * Handles errors by canceling the subscription and closing the stream.
     *
     * @param throwable The {@link Throwable} encountered during stream processing.
     */
    @Override
    public void onError(final Throwable throwable) {
        this.subscription.cancel();
        this.close();
    }

    /**
     * Completes the stream by canceling the subscription.
     */
    @Override
    public void onComplete() {
        this.subscription.cancel();
    }
}
