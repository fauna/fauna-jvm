package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaStream;
import com.fauna.e2e.beans.Product;
import com.fauna.response.QuerySuccess;
import com.fauna.response.StreamEvent;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.fauna.query.builder.Query.fql;

public class E2EStreamingTest {
    public static final FaunaClient client = Fauna.local();

    @BeforeAll
    public static void setup() {
        Fixtures.ProductCollection(client);
    }


    class InventorySubscriber implements Flow.Subscriber<StreamEvent<Product>> {
        private AtomicLong timestamp = new AtomicLong(0);
        private String cursor = null;
        private AtomicInteger events = new AtomicInteger(0);
        Map<String, Integer> inventory = new ConcurrentHashMap<>();
        Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            new Thread(() -> {
                while (true) {
                    this.subscription.request(1);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
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
        }

        @Override
        public void onError(Throwable throwable) {

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
    }

    @Test
    @Ignore
    public void query_streamOfPerson() throws InterruptedException {
        FaunaStream stream = client.stream(fql("Product.all().toStream()"), Product.class);
        InventorySubscriber inventory = new InventorySubscriber();
        stream.subscribe(inventory);
        QuerySuccess<Object> cheese = client.query(fql("Product.create({name: 'cheese', quantity: 1})"));
        QuerySuccess<Object> bread = client.query(fql("Product.create({name: 'bread', quantity: 2})"));
        QuerySuccess<Object> wine = client.query(fql("Product.create({name: 'wine', quantity: 3})"));
        long start = System.currentTimeMillis();
        int events = inventory.countEvents();
        System.out.println("Events: " + events);
        while (System.currentTimeMillis() < start + 5_000) {
            Thread.sleep(10);
            int latest = inventory.countEvents();
            if (latest > events) {
                events = latest;
            }
        }
        inventory.onComplete();


    }
}
