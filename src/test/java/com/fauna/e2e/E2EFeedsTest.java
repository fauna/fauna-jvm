package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;
import com.fauna.client.FeedIterator;
import com.fauna.e2e.beans.Product;
import com.fauna.feed.EventSource;
import com.fauna.feed.FeedOptions;
import com.fauna.feed.FeedPage;
import com.fauna.query.EventSourceResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.response.StreamEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.fauna.client.FaunaConfig.FaunaEndpoint.LOCAL;
import static com.fauna.query.builder.Query.fql;
import static com.fauna.response.StreamEvent.EventType.ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EFeedsTest {

    public static FaunaClient client;
    public static Long productCollectionTs;

    @BeforeAll
    public static void setup() {
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        FaunaConfig config = FaunaConfig.builder().endpoint(LOCAL).secret("secret").build();
        client = Fauna.client(config);
        productCollectionTs = Fixtures.ProductCollection(client);
    }

    @Test
    public void feedOfAll() {
        FeedOptions options = FeedOptions.builder().startTs(productCollectionTs).build();
        FeedIterator<Product> iter = client.feed(fql("Product.all().eventSource()"), options, Product.class);
        List<List<StreamEvent<Product>>> pages = new ArrayList<>();
        iter.forEachRemaining(page -> pages.add(page.getEvents()));
        assertEquals(4, pages.size());
        List<StreamEvent<Product>> products = pages.stream().flatMap(p -> p.stream()).collect(Collectors.toList());
        assertEquals(50, products.size());
    }

    @Test
    public void manualFeed() {
        // Use the feeds API with complete (i.e. manual) control of the calls made to Fauna.
        QuerySuccess<EventSourceResponse> sourceQuery = client.query(fql("Product.all().eventSource()"), EventSourceResponse.class);
        EventSource source = EventSource.fromResponse(sourceQuery.getData());
        List<StreamEvent<Product>> productUpdates = new ArrayList<>();
        FeedOptions initialOptions = FeedOptions.builder().startTs(productCollectionTs).pageSize(2).build();
        CompletableFuture<FeedPage<Product>> pageFuture = client.poll(source, initialOptions, Product.class);
        int pageCount = 0;
        String lastPageCursor = null;

        while(pageFuture != null) {
            // Handle page
            FeedPage<Product> latestPage = pageFuture.join();
            lastPageCursor = latestPage.getCursor();
            productUpdates.addAll(latestPage.getEvents());
            pageCount++;

            // Get next page (if it's not null)
            FeedOptions nextPageOptions = initialOptions.nextPage(latestPage);
            // You can also inspect next
            if (latestPage.hasNext()) {
                pageFuture = client.poll(source, nextPageOptions, Product.class);
            } else {
                pageFuture = null;
            }
        }
        assertEquals(50, productUpdates.size());
        assertEquals(25, pageCount);
        // Because there is no filtering, these cursors are the same.
        // If we filtered events, then the page cursor could be different from the cursor of the last element.
        assertEquals(lastPageCursor, productUpdates.get(productUpdates.size()-1).getCursor());

    }

    @Test
    public void feedError() {
        FeedOptions options = FeedOptions.builder().startTs(0L).build();
        FeedIterator<Product> iter = client.feed(fql("Product.all().eventSource()"), options, Product.class);
        FeedPage<Product> pageOne = iter.next();
        assertFalse(pageOne.hasNext());
        assertEquals(1, pageOne.getEvents().size());
        StreamEvent<Product> errorEvent = pageOne.getEvents().get(0);
        assertEquals(ERROR, errorEvent.getType());
        assertEquals("invalid_stream_start_time", errorEvent.getError().getCode());
        assertTrue(errorEvent.getError().getMessage().contains("is too far in the past"));
    }

    @Test
    public void feedFlattened() {
        FeedOptions options = FeedOptions.builder().startTs(productCollectionTs).build();
        FeedIterator<Product> iter = client.feed(fql("Product.all().eventSource()"), options, Product.class);
        Iterator<StreamEvent<Product>> productIter = iter.flatten();
        List<StreamEvent<Product>> products = new ArrayList<>();
        // Java iterators not being iterable (or useable in a for-each loop) is annoying.
        for (StreamEvent<Product> p : (Iterable<StreamEvent<Product>>) () -> productIter) {
            products.add(p);
        }
        assertEquals(50, products.size());
    }
}
