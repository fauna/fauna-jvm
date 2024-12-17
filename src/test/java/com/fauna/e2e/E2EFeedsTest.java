package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;
import com.fauna.e2e.beans.Product;
import com.fauna.event.EventSource;
import com.fauna.event.FaunaEvent;
import com.fauna.event.FeedIterator;
import com.fauna.event.FeedOptions;
import com.fauna.event.FeedPage;
import com.fauna.exception.InvalidRequestException;
import com.fauna.response.QueryFailure;
import com.fauna.response.QuerySuccess;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.fauna.client.FaunaConfig.FaunaEndpoint.LOCAL;
import static com.fauna.event.FaunaEvent.EventType.ERROR;
import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EFeedsTest {

    public static FaunaClient client;
    public static Long productCollectionTs;

    @BeforeAll
    public static void setup() {
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        FaunaConfig config =
                FaunaConfig.builder().endpoint(LOCAL).secret("secret").build();
        client = Fauna.client(config);
        productCollectionTs = Fixtures.ProductCollection(client);
    }

    @Test
    public void feedOfAll() {
        FeedOptions options =
                FeedOptions.builder().startTs(productCollectionTs).build();
        FeedIterator<Product> iter =
                client.feed(fql("Product.all().eventSource()"), options,
                        Product.class);
        List<List<FaunaEvent<Product>>> pages = new ArrayList<>();
        iter.forEachRemaining(page -> pages.add(page.getEvents()));
        assertEquals(4, pages.size());
        List<FaunaEvent<Product>> products =
                pages.stream().flatMap(p -> p.stream())
                        .collect(Collectors.toList());
        assertEquals(50, products.size());
    }

    @Test
    public void manualFeed() {
        // Use the feeds API with complete (i.e. manual) control of the calls made to Fauna.
        QuerySuccess<EventSource> sourceQuery =
                client.query(fql("Product.all().eventSource()"),
                        EventSource.class);
        EventSource source = sourceQuery.getData();
        List<FaunaEvent<Product>> productUpdates = new ArrayList<>();
        FeedOptions initialOptions =
                FeedOptions.builder().startTs(productCollectionTs).pageSize(2)
                        .build();
        CompletableFuture<FeedPage<Product>> pageFuture =
                client.poll(source, initialOptions, Product.class);
        int pageCount = 0;
        String lastPageCursor = null;

        while (pageFuture != null) {
            // Handle page
            FeedPage<Product> latestPage = pageFuture.join();
            lastPageCursor = latestPage.getCursor();
            System.out.println(lastPageCursor);
            productUpdates.addAll(latestPage.getEvents());
            pageCount++;

            // Get next page (if it's not null)
            FeedOptions nextPageOptions = initialOptions.nextPage(latestPage);
            // You can also inspect next
            if (latestPage.hasNext()) {
                pageFuture =
                        client.poll(source, nextPageOptions, Product.class);
            } else {
                pageFuture = null;
            }
        }
        assertEquals(50, productUpdates.size());
        assertEquals(25, pageCount);
    }

    @Test
    public void feedError() {
        // Fauna can throw a HTTP error in some cases. In this case it's bad request to the feed API. Presumably
        // some of the others like ThrottlingException, and AuthenticationException can also be thrown.
        CompletableFuture<FeedPage<Product>> future =
                client.poll(EventSource.fromToken("badToken"),
                        FeedOptions.DEFAULT, Product.class);
        CompletionException ce =
                assertThrows(CompletionException.class, () -> future.join());
        InvalidRequestException ire = (InvalidRequestException) ce.getCause();

        assertEquals("invalid_request", ire.getErrorCode());
        assertEquals(400, ire.getStatusCode());
        assertEquals(
                "400 (invalid_request): Invalid request body: invalid event source provided.",
                ire.getMessage());
        assertTrue(ire.getTxnTs().isEmpty());
        assertNull(ire.getStats());
        assertNull(ire.getSchemaVersion());
        assertNull(ire.getSummary());
        assertNull(ire.getCause());
        assertNull(ire.getQueryTags());

        QueryFailure failure = ire.getResponse();
        assertTrue(failure.getConstraintFailures().isEmpty());
        assertTrue(failure.getAbort(null).isEmpty());
    }

    @Test
    public void feedEventError() {
        // Fauna can also return a valid feed page, with HTTP 200, but an "error" event type.
        FeedOptions options = FeedOptions.builder().startTs(0L).build();
        QuerySuccess<EventSource> sourceQuery =
                client.query(fql("Product.all().eventSource()"), EventSource.class);
        FeedIterator<Product> iter = client.feed(sourceQuery.getData(), options, Product.class);
        FeedPage<Product> pageOne = iter.next();
        assertFalse(pageOne.hasNext());
        assertEquals(1, pageOne.getEvents().size());
        FaunaEvent<Product> errorEvent = pageOne.getEvents().get(0);
        assertEquals(ERROR, errorEvent.getType());
        assertEquals("invalid_stream_start_time",
                errorEvent.getError().getCode());
        assertTrue(errorEvent.getError().getMessage()
                .contains("is too far in the past"));
    }

    @Test
    public void feedFlattened() {
        FeedOptions options =
                FeedOptions.builder().startTs(productCollectionTs).build();
        FeedIterator<Product> iter =
                client.feed(fql("Product.all().eventSource()"), options,
                        Product.class);
        Iterator<FaunaEvent<Product>> productIter = iter.flatten();
        List<FaunaEvent<Product>> products = new ArrayList<>();
        // Java iterators not being iterable (or useable in a for-each loop) is annoying.
        for (FaunaEvent<Product> p : (Iterable<FaunaEvent<Product>>) () -> productIter) {
            products.add(p);
        }
        assertEquals(50, products.size());
    }
}
