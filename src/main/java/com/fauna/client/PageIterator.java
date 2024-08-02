package com.fauna.client;


import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QuerySuccess;
import com.fauna.serialization.generic.PageOf;
import com.fauna.types.Page;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.fauna.query.builder.Query.fql;

/**
 * PageIterator iterates over paged responses from Fauna, the default page size is 16.
 * @param <E>
 */
public class PageIterator<E> implements Iterator<Page<E>> {
    private final FaunaClient client;
    private final QueryOptions options;
    private final PageOf<E> pageClass;
    private CompletableFuture<QuerySuccess<Page<E>>> latestQuery;
    private boolean hasNext = true;

    /**
     * Construct a new PageIterator.
     * @param client            A client that makes requests to Fauna.
     * @param fql               The FQL query.
     * @param resultClass       The class of the elements returned from Fauna (i.e. the rows).
     * @param options           (optionally) pass in QueryOptions.
     */
    public PageIterator(FaunaClient client, Query fql, Class<E> resultClass, QueryOptions options) {
        this.client = client;
        this.pageClass = new PageOf<>(resultClass);
        this.options = options;
        // Initial query;
        this.latestQuery = client.asyncQuery(fql, this.pageClass, options);
    }

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    /**
     * Get the next Page.
     * @return  The next Page of elements E.
     */
    @Override
    public Page<E> next() {
        if (this.latestQuery == null || !this.hasNext) {
            this.hasNext = false;
            throw new NoSuchElementException();
        }
        try {
            QuerySuccess<Page<E>> latestResult = latestQuery.get();
            Page<E> lastPage = latestResult.getData();
            if (lastPage.after() != null) {
                this.latestQuery = client.asyncQuery(fql("Set.paginate(${after})", Map.of("after", lastPage.after())), pageClass, options);
            } else {
                this.hasNext = false;
                this.latestQuery = null;
            }
            return lastPage;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return an iterator that iterates directly over the items that make up the page contents.
     * @return      An iterator of E.
     */
    public Iterator<E> flatten() {
        return new Iterator<>() {
            private final PageIterator<E> pageIterator = PageIterator.this;
            private Iterator<E> thisPage = pageIterator.next().data().iterator();
            @Override
            public boolean hasNext() {
                return thisPage.hasNext() || pageIterator.hasNext();
            }

            @Override
            public E next() {
                try {
                    return thisPage.next();
                } catch (NoSuchElementException e) {
                    if (pageIterator.hasNext()) {
                        this.thisPage = pageIterator.next().data().iterator();
                        return thisPage.next();
                    } else {
                        throw e;
                    }
                }
            }
        };
    }
}
