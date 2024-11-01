package com.fauna.event;


import com.fauna.client.FaunaClient;
import com.fauna.exception.FaunaException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * FeedIterator iterates over Event Feed pages from Fauna.
 * @param <E>
 */
public class FeedIterator<E> implements Iterator<FeedPage<E>> {
    private final FaunaClient client;
    private final Class<E> resultClass;
    private final EventSource eventSource;
    private FeedOptions latestOptions;
    private CompletableFuture<FeedPage<E>> feedFuture;

    /**
     * Construct a new PageIterator.
     *
     * @param client            A client that makes requests to Fauna.
     * @param eventSource       The Fauna Event Source.
     * @param feedOptions       The FeedOptions object.
     * @param resultClass       The class of the elements returned from Fauna (i.e. the rows).
     */
    public FeedIterator(FaunaClient client, EventSource eventSource, FeedOptions feedOptions, Class<E> resultClass) {
        this.client = client;
        this.resultClass = resultClass;
        this.eventSource = eventSource;
        this.latestOptions = feedOptions;
        this.feedFuture = client.poll(eventSource, feedOptions, resultClass);
    }

    @Override
    public boolean hasNext() {
        return this.feedFuture != null;
    }

    /**
     * Returns a CompletableFuture that will complete with the next page (or throw a FaunaException).
     * When the future completes, the next page will be fetched in the background.
     *
     * @return  A CompletableFuture that completes with a new FeedPage instance.
     */
    public CompletableFuture<FeedPage<E>> nextAsync() {
        if (this.feedFuture != null) {
            return this.feedFuture.thenApply(fs -> {
                if (fs.hasNext()) {
                    FeedOptions options = this.latestOptions.nextPage(fs);
                    this.latestOptions = options;
                    this.feedFuture = client.poll(this.eventSource, options, resultClass);
                } else {
                    this.feedFuture = null;
                }
                return fs;
            });
        } else {
            throw new NoSuchElementException();
        }
    }


    /**
     * Get the next Page (synchronously).
     * @return  FeedPage        The next Page of elements E.
     * @throws  FaunaException  If there is an error getting the next page.
     */
    @Override
    public FeedPage<E> next() {
        try {
            return nextAsync().join();
        } catch (CompletionException ce) {
            if (ce.getCause() != null && ce.getCause() instanceof FaunaException) {
                throw (FaunaException) ce.getCause();
            } else {
                throw ce;
            }
        }
    }

    /**
     * Return an iterator that iterates directly over the items that make up the page contents.
     * @return      An iterator of E.
     */
    public Iterator<FaunaEvent<E>> flatten() {
        return new Iterator<>() {
            private final FeedIterator<E> feedIterator = FeedIterator.this;
            private Iterator<FaunaEvent<E>> thisPage = feedIterator.hasNext() ? feedIterator.next().getEvents().iterator() : null;
            @Override
            public boolean hasNext() {
                return thisPage != null && (thisPage.hasNext() || feedIterator.hasNext());
            }

            @Override
            public FaunaEvent<E> next() {
                if (thisPage == null) {
                    throw new NoSuchElementException();
                }
                try {
                    return thisPage.next();
                } catch (NoSuchElementException e) {
                    if (feedIterator.hasNext()) {
                        this.thisPage = feedIterator.next().getEvents().iterator();
                        return thisPage.next();
                    } else {
                        throw e;
                    }
                }
            }
        };
    }
}
