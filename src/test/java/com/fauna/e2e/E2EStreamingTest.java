package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaStream;
import com.fauna.e2e.beans.Product;
import com.fauna.response.StreamEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EStreamingTest {
    public static final FaunaClient client = Fauna.local();

    @BeforeAll
    public static void setup() {
        Fixtures.ProductCollection(client);
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
            System.out.println(MessageFormat.format("Event {0}, {1}", event.getCursor(), event.getTimestamp()));
            events.addAndGet(1);
            event.getData().ifPresent(product -> inventory.put(product.getName(), product.getQuantity()));
            // Fauna delivers events to the client in order, but it's up to the user to keep those events in order.
            this.timestamp.updateAndGet(value -> value < event.getTimestamp() ? value : event.getTimestamp());
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
    public void query_streamOfPerson() throws InterruptedException {
        FaunaStream stream = client.stream(fql("Product.all().toStream()"), Product.class);
        InventorySubscriber inventory = new InventorySubscriber();
        stream.subscribe(inventory);
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
    }
}
