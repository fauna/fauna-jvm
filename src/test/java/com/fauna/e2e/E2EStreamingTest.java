package com.fauna.e2e;

import com.fauna.beans.InventorySource;
import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.PageIterator;
import com.fauna.client.QueryStatsSummary;
import com.fauna.e2e.beans.Product;
import com.fauna.event.EventSource;
import com.fauna.event.FaunaEvent;
import com.fauna.event.FaunaStream;
import com.fauna.event.StreamOptions;
import com.fauna.exception.ClientException;
import com.fauna.exception.NullDocumentException;
import com.fauna.query.QueryOptions;
import com.fauna.query.builder.Query;
import com.fauna.response.QuerySuccess;
import com.fauna.types.NullableDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpTimeoutException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fauna.codec.Generic.nullableDocumentOf;
import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EStreamingTest {
    public static final FaunaClient client = Fauna.local();
    private static final Random random = new Random();
    private static final String candidateChars =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    @BeforeAll
    public static void setup() {
        Fixtures.ProductCollection(client);
    }

    public static String randomName(int length) {
        return Stream.generate(
                        () -> candidateChars.charAt(
                                random.nextInt(candidateChars.length()))).map(
                        c -> Character.toString(c)).limit(length)
                .collect(Collectors.joining());
    }

    public static Query createProduct() {
        return fql("Product.create({name: ${name}, quantity: ${quantity}})",
                Map.of("name", randomName(5), "quantity",
                        random.nextInt(1, 16)));
    }

    public static void waitForSync(InventorySubscriber watcher)
            throws InterruptedException {
        long start = System.currentTimeMillis();
        int events = watcher.countEvents();
        while (System.currentTimeMillis() < start + 2_000) {
            Thread.sleep(10);
            int latest = watcher.countEvents();
            if (latest > events) {
                events = latest;
            }
        }

    }

    private static Long doDelete(String name) {
        Query query = fql("Product.firstWhere(.name == ${name})!.delete()",
                Map.of("name", name));
        QuerySuccess<NullableDocument<Product>> success =
                client.query(query, nullableDocumentOf(Product.class));
        NullableDocument<Product> nullDoc = success.getData();
        assertThrows(NullDocumentException.class, () -> nullDoc.get());
        return success.getLastSeenTxn();
    }

    static class InventorySubscriber
            implements Flow.Subscriber<FaunaEvent<Product>> {
        private final AtomicLong timestamp = new AtomicLong(0);
        private String cursor = null;
        private final AtomicInteger events = new AtomicInteger(0);
        private final Map<String, Integer> inventory;
        Flow.Subscription subscription;

        public InventorySubscriber(Map<String, Integer> inventory) {
            this.inventory = inventory;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(FaunaEvent<Product> event) {
            events.addAndGet(1);
            if (event.getType().equals(FaunaEvent.EventType.ADD) ||
                    event.getType().equals(FaunaEvent.EventType.UPDATE)) {
                event.getData().ifPresent(
                        product -> inventory.put(product.getName(),
                                product.getQuantity()));
            } else if (event.getType().equals(FaunaEvent.EventType.REMOVE)) {
                event.getData().ifPresent(
                        product -> inventory.remove(product.getName()));
            }
            // Fauna delivers events to the client in order, but it's up to the user to keep those events in order.
            event.getTimestamp().ifPresent(ts -> this.timestamp.updateAndGet(
                    value -> value < ts ? value : ts));
            this.cursor = event.getCursor();
            System.out.println("Total inventory: " + this.countInventory());
            this.subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println("Oopsie: " + throwable.getMessage());
        }

        @Override
        public void onComplete() {
            if (this.subscription != null) {
                this.subscription.cancel();
            }
        }

        public int countInventory() {
            return this.inventory.values().stream().reduce(0, Integer::sum);
        }

        public int countEvents() {
            return events.get();
        }

        public String status() {
            return MessageFormat.format(
                    "Processed {0} events, inventory {1} at cursor/timestamp: {2}/{3}",
                    countEvents(), countInventory(), this.cursor,
                    this.timestamp.get());
        }

    }

    @Test
    public void query_streamOfProduct() throws InterruptedException {
        QueryStatsSummary initialStats = client.getStatsCollector().read();
        // Initialize stream outside try-with-resources so we can assert that it's closed at the end of this test;.
        FaunaStream<Product> stream =
                client.stream(fql("Product.all().eventSource()"),
                        Product.class);
        try (stream) {
            InventorySubscriber watcher =
                    new InventorySubscriber(new ConcurrentHashMap<>());
            stream.subscribe(watcher);
            assertFalse(stream.isClosed());

            doDelete("product-2");

            waitForSync(watcher);
            watcher.onComplete();
            assertFalse(stream.isClosed());
        }
        stream.close();
        assertTrue(stream.isClosed());
    }

    @Test
    public void query_trackStateWithStream() throws InterruptedException {
        // This test demonstrates querying the current state of a collection, and then tracking the changes from
        // that moment on, which guarantees that no updates will be missed.
        QueryStatsSummary initialStats = client.getStatsCollector().read();
        QuerySuccess<InventorySource> success = client.query(
                fql("let products = Product.all()\n{ firstPage: products.pageSize(4), eventSource: products.eventSource()}"),
                InventorySource.class);
        InventorySource inventorySource = success.getData();

        // First, we process all the products that existed when the query was made.
        PageIterator<Product> pageIterator =
                new PageIterator<>(client, inventorySource.firstPage,
                        Product.class, QueryOptions.getDefault());
        Map<String, Integer> inventory = new HashMap<>();
        List<Product> products = new ArrayList<>();
        pageIterator.flatten().forEachRemaining(product -> {
            products.add(product);
            inventory.put(product.getName(), product.getQuantity());
        });

        // Now start a stream based on same query, and it's transaction timestamp.
        EventSource eventSource = inventorySource.eventSource;
        StreamOptions streamOptions =
                StreamOptions.builder().startTimestamp(success.getLastSeenTxn())
                        .build();
        FaunaStream<Product> stream =
                client.stream(eventSource, streamOptions, Product.class);
        try (stream) {
            InventorySubscriber watcher = new InventorySubscriber(inventory);
            stream.subscribe(watcher);
            assertFalse(stream.isClosed());
            products.add(client.query(
                    fql("Product.create({name: 'bread', quantity: 2})"),
                    Product.class).getData());
            products.add(client.query(
                    fql("Product.firstWhere(.name == \"product-0\")!.update({quantity: 30})"),
                    Product.class).getData());
            waitForSync(watcher);
            int before = watcher.countInventory();

            client.query(fql("Product.create({name: 'cheese', quantity: 17})"),
                    Product.class).getData();
            waitForSync(watcher);
            assertEquals(before + 17, watcher.countInventory());

            doDelete("cheese");
            waitForSync(watcher);
            assertEquals(before, watcher.countInventory());

            watcher.onComplete();
            Integer total = products.stream().map(Product::getQuantity)
                    .reduce(0, Integer::sum);
            assertEquals(total, watcher.countInventory());
            assertFalse(stream.isClosed());
        }
        stream.close();
        assertTrue(stream.isClosed());

        QueryStatsSummary finalStats = client.getStatsCollector().read();
        assertTrue(finalStats.getReadOps() > initialStats.getReadOps());
        assertTrue(finalStats.getComputeOps() > initialStats.getComputeOps());
    }

    @Test
    public void handleStreamError() throws InterruptedException {
        // It would be nice to have another test that generates a stream with normal events, and then an error
        // event, but this at least tests some of the functionality.
        QuerySuccess<EventSource> queryResp =
                client.query(fql("Product.all().eventSource()"),
                        EventSource.class);
        EventSource source = queryResp.getData();
        StreamOptions options =
                StreamOptions.builder().cursor("invalid_cursor").build();
        FaunaStream<Product> stream =
                client.stream(source, options, Product.class);
        InventorySubscriber inventory =
                new InventorySubscriber(new ConcurrentHashMap<>());
        stream.subscribe(inventory);
        long start = System.currentTimeMillis();
        while (!stream.isClosed() &&
                System.currentTimeMillis() < (start + 5_000)) {
            Thread.sleep(100);
        }
        assertTrue(stream.isClosed());
    }

    @Test
    public void handleStreamTimeout() {
        QuerySuccess<EventSource> queryResp = client.query(
                fql("Product.all().eventSource()"), EventSource.class);
        StreamOptions options =
                StreamOptions.builder().timeout(Duration.ofMillis(1)).build();
        ClientException exc = assertThrows(ClientException.class,
                () -> client.stream(queryResp.getData(), options, Product.class));
        assertEquals(ExecutionException.class, exc.getCause().getClass());
        assertEquals(HttpTimeoutException.class,
                exc.getCause().getCause().getClass());
    }

    @Disabled("Will fix this for GA, I think the other drivers have this bug too.")
    @Test
    public void handleLargeEvents() throws InterruptedException {
        InventorySubscriber inventory;
        try (FaunaStream<Product> stream = client.stream(
                fql("Product.all().eventSource()"), Product.class)) {
            inventory = new InventorySubscriber(new ConcurrentHashMap<>());
            stream.subscribe(inventory);
        }
        List<Product> products = new ArrayList<>();

        byte[] image = new byte[20];
        random.nextBytes(image);
        // Product cheese = new Product("cheese", 1, image);
        StringBuilder fifteenKName = new StringBuilder();
        for (int i = 0; i < 1024 * 15; i++) {
            fifteenKName.append(candidateChars.charAt(
                    random.nextInt(candidateChars.length())));
        }
        assertEquals(fifteenKName.length(), 15360); // 15k string works.
        products.add(
                client.query(fql("Product.create({name: ${name}, quantity: 1})",
                                Map.of("name", fifteenKName.toString())), Product.class)
                        .getData());

        StringBuilder sixteenKName = new StringBuilder();
        for (int i = 0; i < 1024 * 16; i++) {
            sixteenKName.append(candidateChars.charAt(
                    random.nextInt(candidateChars.length())));
        }
        assertEquals(sixteenKName.length(), 16384);

        // 16k string causes the stream to throw.
        // FaunaStream onError: com.fasterxml.jackson.databind.JsonMappingException: Unexpected end-of-input: was
        // expecting closing quote for a string value at [Source: ...
        products.add(
                client.query(fql("Product.create({name: ${name}, quantity: 1})",
                                Map.of("name", sixteenKName.toString())), Product.class)
                        .getData());

        long start = System.currentTimeMillis();
        int events = inventory.countEvents();
        System.out.println("Events: " + events);
        while (System.currentTimeMillis() < start + 2_000) {
            Thread.sleep(10);
            int latest = inventory.countEvents();
            if (latest > events) {
                events = latest;
            }
        }
        inventory.onComplete();
        System.out.println(inventory.status());
        Integer total = products.stream().map(Product::getQuantity)
                .reduce(0, Integer::sum);
        assertEquals(total, inventory.countInventory());
    }

    @Disabled("This test sometimes causes Fauna to generate an error for getting too far behind.")
    @Test
    public void handleManyEvents() throws InterruptedException {
        FaunaStream stream = client.stream(fql("Product.all().eventSource()"),
                Product.class);
        InventorySubscriber inventory =
                new InventorySubscriber(new ConcurrentHashMap<>());

        stream.subscribe(inventory);
        List<CompletableFuture<Product>> productFutures = new ArrayList<>();
        // FaunaStream onError: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:
        // Unrecognized field "error" (class com.fauna.response.wire.StreamEventWire), not marked as ignorable (5 known properties: "data", "txn_ts", "cursor", "stats", "type"])
        // at [Source: (String)"{"type":"error","error":{"code":"stream_overflow","message":"Too many events to process."},"stats":{"read_ops":0,"storage_bytes_read":0,"compute_ops":0,"processing_time_ms":0,"rate_limits_hit":[]}}
        //"; line: 1, column: 26] (through reference chain: com.fauna.response.wire.StreamEventWire["error"])
        Stream.generate(E2EStreamingTest::createProduct).limit(10_000).forEach(
                fql -> productFutures.add(client.asyncQuery(fql, Product.class)
                        .thenApply(success -> success.getData())));
        Thread.sleep(60_000);

        int totalInventory =
                productFutures.stream().map(p -> p.join().getQuantity())
                        .reduce(0, Integer::sum);
        assertEquals(totalInventory, inventory.countInventory());
    }

}
