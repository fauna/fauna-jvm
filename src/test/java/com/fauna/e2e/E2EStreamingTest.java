package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaStream;
import com.fauna.e2e.beans.Product;
import com.fauna.query.StreamTokenResponse;
import com.fauna.query.builder.Query;
import com.fauna.response.QuerySuccess;
import com.fauna.response.StreamEvent;
import com.fauna.stream.StreamRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EStreamingTest {
    public static final FaunaClient client = Fauna.local();
    private static final Random random = new Random();
    private static final String candidateChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    @BeforeAll
    public static void setup() {
        Fixtures.ProductCollection(client);
    }

    public static String randomName(int length) {
        return Stream.generate(
                () -> candidateChars.charAt(random.nextInt(candidateChars.length()))).map(
                        c -> Character.toString(c)).limit(length).collect(Collectors.joining());
    }

    public static Query createProduct() {
        return fql("Product.create({name: ${name}, quantity: ${quantity}})",
                Map.of("name", randomName(5), "quantity", random.nextInt(1, 16)));
    }


    static class InventorySubscriber implements Flow.Subscriber<StreamEvent<Product>> {
        private final AtomicLong timestamp = new AtomicLong(0);
        private String cursor = null;
        private final AtomicInteger events = new AtomicInteger(0);
        Map<String, Integer> inventory = new ConcurrentHashMap<>();
        Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(StreamEvent<Product> event) {
            System.out.println(MessageFormat.format("Event {0}, {1}", event.getCursor(), event.getTimestamp().orElse(-1L)));
            events.addAndGet(1);
            event.getData().ifPresent(product -> inventory.put(product.getName(), product.getQuantity()));
            // Fauna delivers events to the client in order, but it's up to the user to keep those events in order.
            event.getTimestamp().ifPresent(ts -> this.timestamp.updateAndGet(value -> value < ts ? value : ts));
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
                    countEvents(), countInventory(), this.cursor, this.timestamp.get());
        }

    }

    @Test
    public void query_streamOfProduct() throws InterruptedException {
        FaunaStream stream = client.stream(fql("Product.all().toStream()"), Product.class);
        InventorySubscriber inventory = new InventorySubscriber();
        stream.subscribe(inventory);
        assertFalse(stream.isClosed());
        List<Product> products = new ArrayList<>();
        products.add(client.query(fql("Product.create({name: 'cheese', quantity: 1})"), Product.class).getData());
        products.add(client.query(fql("Product.create({name: 'bread', quantity: 2})"), Product.class).getData());
        products.add(client.query(fql("Product.create({name: 'wine', quantity: 3})"), Product.class).getData());
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
        Integer total = products.stream().map(Product::getQuantity).reduce(0, Integer::sum);
        assertEquals(total, inventory.countInventory());
        assertFalse(stream.isClosed());
        stream.close();
        assertTrue(stream.isClosed());
    }

    @Test
    public void handleStreamError() throws InterruptedException {
        // It would be nice to have another test that generates a stream with normal events, and then an error
        // event, but this at least tests some of the functionality.
        QuerySuccess<StreamTokenResponse> queryResp = client.query(fql("Product.all().toStream()"), StreamTokenResponse.class);
        StreamRequest request = new StreamRequest(queryResp.getData().getToken(), "invalid_cursor");
        FaunaStream stream = client.stream(request, Product.class);
        InventorySubscriber inventory = new InventorySubscriber();
        stream.subscribe(inventory);
        long start = System.currentTimeMillis();
        while (!stream.isClosed() && System.currentTimeMillis() < (start + 5_000)) {
            Thread.sleep(100);
        }
        assertTrue(stream.isClosed());
    }

    @Disabled("Will fix this for GA, I think the other drivers have this bug too.")
    @Test
    public void handleLargeEvents() throws InterruptedException {
        FaunaStream stream = client.stream(fql("Product.all().toStream()"), Product.class);
        InventorySubscriber inventory = new InventorySubscriber();
        stream.subscribe(inventory);
        List<Product> products = new ArrayList<>();

        byte[] image = new byte[20];
        random.nextBytes(image);
        // Product cheese = new Product("cheese", 1, image);
        StringBuilder fifteenKName = new StringBuilder();
        for (int i = 0; i < 1024 * 15; i++) {
            fifteenKName.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
        }
        assertEquals(fifteenKName.length(), 15360); // 15k string works.
        products.add(client.query(fql("Product.create({name: ${name}, quantity: 1})",
                Map.of("name", fifteenKName.toString())), Product.class).getData());

        StringBuilder sixteenKName = new StringBuilder();
        for (int i = 0; i < 1024 * 16; i++) {
            sixteenKName.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
        }
        assertEquals(sixteenKName.length(), 16384);

        // 16k string causes the stream to throw.
        // FaunaStream onError: com.fasterxml.jackson.databind.JsonMappingException: Unexpected end-of-input: was
        // expecting closing quote for a string value at [Source: ...
        products.add(client.query(fql("Product.create({name: ${name}, quantity: 1})",
                Map.of("name", sixteenKName.toString())), Product.class).getData());

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
        Integer total = products.stream().map(Product::getQuantity).reduce(0, Integer::sum);
        assertEquals(total, inventory.countInventory());
    }

    @Disabled("This test sometimes causes Fauna to generate an error for getting too far behind.")
    @Test
    public void handleManyEvents() throws InterruptedException {
        FaunaStream stream = client.stream(fql("Product.all().toStream()"), Product.class);
        InventorySubscriber inventory = new InventorySubscriber();

        stream.subscribe(inventory);
        List<CompletableFuture<Product>> productFutures = new ArrayList<>();
        // FaunaStream onError: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:
        // Unrecognized field "error" (class com.fauna.response.wire.StreamEventWire), not marked as ignorable (5 known properties: "data", "txn_ts", "cursor", "stats", "type"])
        // at [Source: (String)"{"type":"error","error":{"code":"stream_overflow","message":"Too many events to process."},"stats":{"read_ops":0,"storage_bytes_read":0,"compute_ops":0,"processing_time_ms":0,"rate_limits_hit":[]}}
        //"; line: 1, column: 26] (through reference chain: com.fauna.response.wire.StreamEventWire["error"])
        Stream.generate(E2EStreamingTest::createProduct).limit(10_000).forEach(
                fql -> productFutures.add(client.asyncQuery(fql, Product.class).thenApply(success -> success.getData())));
        Thread.sleep(60_000);

        int totalInventory = productFutures.stream().map(p -> p.join().getQuantity()).reduce(0, Integer::sum);
        assertEquals(totalInventory, inventory.countInventory());
    }

}
