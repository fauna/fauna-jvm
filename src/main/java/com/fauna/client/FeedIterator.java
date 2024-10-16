package com.fauna.client;


import com.fauna.exception.FaunaException;
import com.fauna.feed.FeedRequest;
import com.fauna.feed.FeedSuccess;
import com.fauna.response.StreamEvent;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * FeedIterator iterates over Event Feed pages from Fauna.
 * @param <E>
 */
public class FeedIterator<E> implements Iterator<FeedSuccess<E>> {
    private final FaunaClient client;
    private final Class<E> resultClass;
    private final FeedRequest initialRequest;
    private CompletableFuture<FeedSuccess<E>> feedFuture;

    /**
     * Construct a new PageIterator.
     * @param client            A client that makes requests to Fauna.
     * @param request           The Feed Request object.
     * @param resultClass       The class of the elements returned from Fauna (i.e. the rows).
     */
    public FeedIterator(FaunaClient client, FeedRequest request, Class<E> resultClass) {
        this.client = client;
        this.resultClass = resultClass;
        this.initialRequest = request;
        this.feedFuture = client.asyncFeed(request, resultClass);
    }

    @Override
    public boolean hasNext() {
        return this.feedFuture != null;
    }

    /**
     * Returns a CompletableFuture that will complete with the next page (or throw a FaunaException).
     * @return  A c
     */
    public CompletableFuture<FeedSuccess<E>> nextAsync() {
        if (this.feedFuture != null) {
            return this.feedFuture.thenApply(fs -> {
                if (fs.hasNext()) {
                    FeedRequest.Builder builder = FeedRequest.builder(initialRequest.getToken()).cursor(fs.getCursor());
                    initialRequest.getPageSize().ifPresent(builder::pageSize);
                    FeedRequest request = FeedRequest.builder(initialRequest.getToken()).cursor(fs.getCursor()).build();
                    this.feedFuture = client.asyncFeed(builder.build(), resultClass);
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
     * Get the next Page.
     * @return                  The next Page of elements E.
     * @throws  FaunaException  If there is an error getting the next page.
     */
    @Override
    public FeedSuccess<E> next() {
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
    public Iterator<StreamEvent<E>> flatten() {
        return new Iterator<>() {
            private final FeedIterator<E> feedIterator = FeedIterator.this;
            private Iterator<StreamEvent<E>> thisPage = feedIterator.hasNext() ? feedIterator.next().getEvents().iterator() : null;
            @Override
            public boolean hasNext() {
                return thisPage != null && (thisPage.hasNext() || feedIterator.hasNext());
            }

            @Override
            public StreamEvent<E> next() {
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
