package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;
import com.fauna.client.FeedIterator;
import com.fauna.e2e.beans.Product;
import com.fauna.feed.FeedSuccess;
import com.fauna.response.StreamEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        FeedIterator<Product> iter = client.feed(fql("Product.all().toStream()"), productCollectionTs, Product.class);
        List<List<StreamEvent<Product>>> pages = new ArrayList<>();
        iter.forEachRemaining(page -> pages.add(page.getEvents()));
        assertEquals(4, pages.size());
        List<StreamEvent<Product>> products = pages.stream().flatMap(p -> p.stream()).collect(Collectors.toList());
        assertEquals(50, products.size());
    }

    @Test
    public void feedError() {
        FeedIterator<Product> iter = client.feed(fql("Product.all().eventSource()"), 0L, Product.class);
        FeedSuccess<Product> pageOne = iter.next();
        assertFalse(pageOne.hasNext());
        assertEquals(1, pageOne.getEvents().size());
        StreamEvent errorEvent = pageOne.getEvents().get(0);
        assertEquals(ERROR, errorEvent.getType());
        assertEquals("invalid_stream_start_time", errorEvent.getError().getCode());
        assertTrue(errorEvent.getError().getMessage().contains("is too far in the past"));
    }

    @Test
    public void feedFlattened() {
        FeedIterator<Product> iter = client.feed(fql("Product.all().eventSource()"), productCollectionTs, Product.class);
        Iterator<StreamEvent<Product>> productIter = iter.flatten();
        List<StreamEvent<Product>> products = new ArrayList<>();
        // Java iterators not being iterable (or useable in a for-each loop) is annoying.
        for (StreamEvent<Product> p : (Iterable<StreamEvent<Product>>) () -> productIter) {
            products.add(p);
        }
        assertEquals(50, products.size());
    }
}
