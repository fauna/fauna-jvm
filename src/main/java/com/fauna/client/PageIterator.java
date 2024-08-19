package com.fauna.client;


import com.fauna.exception.FaunaException;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QuerySuccess;
import com.fauna.codec.PageOf;
import com.fauna.types.Page;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.fauna.query.builder.Query.fql;

/**
 * PageIterator iterates over paged responses from Fauna, the default page size is 16.
 * @param <E>
 */
public class PageIterator<E> implements Iterator<Page<E>> {
    private static final String PAGINATE_QUERY = "Set.paginate(${after})";
    private final FaunaClient client;
    private final QueryOptions options;
    private final PageOf<E> pageClass;
    private CompletableFuture<QuerySuccess<Page<E>>> queryFuture;
    private Page<E> latestResult;

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
        this.queryFuture = client.asyncQuery(fql, this.pageClass, options);
        this.latestResult = null;
    }

    public PageIterator(FaunaClient client, Page<E> page, Class<E> resultClass, QueryOptions options) {
        this.client = client;
        this.pageClass = new PageOf<>(resultClass);
        this.options = options;
        this.queryFuture = null;
        this.latestResult = page;
    }

    private void completeFuture() {
        if (this.queryFuture != null && this.latestResult == null) {
            try {
                this.latestResult = queryFuture.join().getData();
            } catch (CompletionException e) {
                if (e.getCause() != null && e.getCause() instanceof FaunaException) {
                    throw (FaunaException) e.getCause();
                } else {
                    throw e;
                }
            }
            this.queryFuture = null;
        }
    }

    @Override
    public boolean hasNext() {
        completeFuture();
        return this.latestResult != null;
    }



    /**
     * Get the next Page.
     * @return  The next Page of elements E.
     */
    @Override
    public Page<E> next() {
        completeFuture();
        if (this.latestResult != null) {
            Page<E> page = this.latestResult;
            this.latestResult = null;
            if (page.after() != null) {
                Map<String, Object> args = Map.of("after", page.after());
                this.queryFuture = client.asyncQuery(fql(PAGINATE_QUERY, args), pageClass, options);
            }
            return page;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Return an iterator that iterates directly over the items that make up the page contents.
     * @return      An iterator of E.
     */
    public Iterator<E> flatten() {
        return new Iterator<>() {
            private final PageIterator<E> pageIterator = PageIterator.this;
            private Iterator<E> thisPage = pageIterator.hasNext() ? pageIterator.next().data().iterator() : null;
            @Override
            public boolean hasNext() {
                return thisPage != null && (thisPage.hasNext() || pageIterator.hasNext());
            }

            @Override
            public E next() {
                if (thisPage == null) {
                    throw new NoSuchElementException();
                }
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
