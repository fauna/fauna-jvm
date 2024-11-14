package com.fauna.client;

import com.fauna.codec.PageOf;
import com.fauna.exception.FaunaException;
import com.fauna.query.AfterToken;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QuerySuccess;
import com.fauna.types.Page;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.fauna.query.builder.Query.fql;

/**
 * PageIterator iterates over paged responses from Fauna, the default page size is 16.
 *
 * @param <E> The type of elements in the page.
 */
public class PageIterator<E> implements Iterator<Page<E>> {
    static final String TOKEN_NAME = "token";
    static final String PAGINATE_QUERY = "Set.paginate(${" + TOKEN_NAME + "})";
    private final FaunaClient client;
    private final QueryOptions options;
    private final PageOf<E> pageClass;
    private CompletableFuture<QuerySuccess<Page<E>>> queryFuture;

    /**
     * Construct a new PageIterator.
     *
     * @param client      A client that makes requests to Fauna.
     * @param fql         The FQL query.
     * @param resultClass The class of the elements returned from Fauna (i.e., the rows).
     * @param options     (optionally) pass in QueryOptions.
     */
    public PageIterator(final FaunaClient client, final Query fql, final Class<E> resultClass,
                        final QueryOptions options) {
        this.client = client;
        this.pageClass = new PageOf<>(resultClass);
        this.options = options;
        // Initial query;
        this.queryFuture = client.asyncQuery(fql, this.pageClass, options);
    }

    /**
     * Construct a new PageIterator starting from a given page.
     *
     * @param client      A client that makes requests to Fauna.
     * @param firstPage   The first Page of elements.
     * @param resultClass The class of the elements returned from Fauna (i.e., the rows).
     * @param options     (optionally) pass in QueryOptions.
     */
    public PageIterator(final FaunaClient client, final Page<E> firstPage,
                        final Class<E> resultClass, final QueryOptions options) {
        this.client = client;
        this.pageClass = new PageOf<>(resultClass);
        this.options = options;
        firstPage.getAfter()
                .ifPresentOrElse(this::doPaginatedQuery, this::endPagination);
    }

    /**
     * Check if there is a next page available.
     *
     * @return True if there is a next page, false otherwise.
     */
    @Override
    public boolean hasNext() {
        return this.queryFuture != null;
    }

    /**
     * Build the page query with a specific AfterToken.
     *
     * @param afterToken The token indicating where the next page should start.
     * @return A Query to fetch the next page.
     */
    public static Query buildPageQuery(final AfterToken afterToken) {
        return fql(PAGINATE_QUERY, Map.of(TOKEN_NAME, afterToken.getToken()));
    }

    /**
     * Performs a paginated query with the provided AfterToken.
     *
     * @param afterToken The token indicating where the next page should start.
     */
    private void doPaginatedQuery(final AfterToken afterToken) {
        this.queryFuture =
                client.asyncQuery(PageIterator.buildPageQuery(afterToken),
                        pageClass, options);
    }

    /**
     * Ends the pagination process when no further pages are available.
     */
    private void endPagination() {
        this.queryFuture = null;
    }

    /**
     * Returns a CompletableFuture that will complete with the next page (or throw a FaunaException).
     *
     * @return A CompletableFuture representing the next page of elements.
     */
    public CompletableFuture<Page<E>> nextAsync() {
        if (this.queryFuture != null) {
            return this.queryFuture.thenApply(qs -> {
                Page<E> page = qs.getData();
                page.getAfter().ifPresentOrElse(this::doPaginatedQuery,
                        this::endPagination);
                return page;
            });
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Get the next Page.
     *
     * @return The next Page of elements E.
     * @throws FaunaException If there is an error getting the next page.
     */
    @Override
    public Page<E> next() {
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
     *
     * @return An iterator of E.
     */
    public Iterator<E> flatten() {
        return new Iterator<>() {
            private final PageIterator<E> pageIterator = PageIterator.this;
            private Iterator<E> thisPage = pageIterator.hasNext()
                    ? pageIterator.next().getData().iterator()
                    : null;

            /**
             * Check if there are more items to iterate over.
             *
             * @return True if there are more items, false otherwise.
             */
            @Override
            public boolean hasNext() {
                return thisPage != null && (thisPage.hasNext() || pageIterator.hasNext());
            }

            /**
             * Get the next item in the iteration.
             *
             * @return The next item in the iteration.
             * @throws NoSuchElementException if no more items are available.
             */
            @Override
            public E next() {
                if (thisPage == null) {
                    throw new NoSuchElementException();
                }
                try {
                    return thisPage.next();
                } catch (NoSuchElementException e) {
                    if (pageIterator.hasNext()) {
                        this.thisPage =
                                pageIterator.next().getData().iterator();
                        return thisPage.next();
                    } else {
                        throw e;
                    }
                }
            }
        };
    }
}
