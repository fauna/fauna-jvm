# Official JVM Driver for [Fauna v10](https://fauna.com) (current - beta)

> [!CAUTION]
> This driver is currently in beta and should not be used in production.

The Fauna JVM driver is a lightweight, open-source wrapper for Fauna's [HTTP
API](https://docs.fauna.com/fauna/current/reference/http/reference/). You can
use the driver to run FQL queries and get results from a Java application.

See the [Fauna docs](https://docs.fauna.com/fauna/current/) for
additional information on how to configure and query your databases.

This driver can only be used with FQL v10 and is not compatible with earlier
versions of FQL. To query your databases with earlier API versions, use the
[faunadb-jvm](https://github.com/fauna/faunadb-jvm) driver.


## Requirements

- Java 11 or later


## API reference

API reference documentation for the driver is available at
https://fauna.github.io/fauna-jvm/. The docs are generated using Javadoc.


## Installation

The driver is available on the [Maven central
repository](https://central.sonatype.com/artifact/com.fauna/fauna-jvm).
You can add the driver to your Java project using Gradle or Maven.


### Gradle

File `build.gradle`:
```
dependencies {
    ...
    implementation "com.fauna:fauna-jvm:X.Y.Z"
    ...
}
```


### Maven

File `fauna-java/pom.xml`:
```xml
<dependencies>
    ...
    <dependency>
      <groupId>com.fauna</groupId>
      <artifactId>fauna-jvm</artifactId>
      <version>X.Y.Z</version>
    </dependency>
    ...
</dependencies>
```


## Basic usage

The following application:

* Initializes a client instance to connect to Fauna.
* Composes a basic FQL query using an FQL template.
* Runs the query using `query()` and `asyncQuery()`.

```java
package org.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.exception.FaunaException;
import com.fauna.query.builder.Query;
import com.fauna.response.QuerySuccess;
import com.fauna.types.Page;

import static com.fauna.codec.Generic.pageOf;
import static com.fauna.query.builder.Query.fql;


public class App {

    // Define class for `Product` documents
    // in expected results.
    public static class Product {
        public String name;

        public String description;

        public Integer price;
    }

    public static void main(String[] args) {
        try {
            // Initialize a default client.
            // It will get the secret from the $FAUNA_SECRET environment variable.
            FaunaClient client = Fauna.client();

            // Compose a query.
            Query query = fql("""
                Product.sortedByPriceLowToHigh() {
                    name,
                    description,
                    price
                }
            """);

            // Run the query synchronously.
            System.out.println("Running synchronous query:");
            runSynchronousQuery(client, query);

            // Run the query asynchronously.
            System.out.println("\nRunning asynchronous query:");
            runAsynchronousQuery(client, query);
        } catch (FaunaException e) {
            System.err.println("Fauna error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    private static void runSynchronousQuery(FaunaClient client, Query query) throws FaunaException {
        // Use `query()` to run a synchronous query.
        // Synchronous queries block the current thread until the query completes.
        // Accepts the query, expected result class, and a nullable set of query options.
        QuerySuccess<Page<Product>> result = client.query(query, pageOf(Product.class));
        printResults(result.getData());
    }

    private static void runAsynchronousQuery(FaunaClient client, Query query) throws ExecutionException, InterruptedException {
        // Use `asyncQuery()` to run an asynchronous, non-blocking query.
        // Accepts the query, expected result class, and a nullable set of query options.
        CompletableFuture<QuerySuccess<Page<Product>>> futureResult = client.asyncQuery(query, pageOf(Product.class));

        QuerySuccess<Page<Product>> result = futureResult.get();
        printResults(result.getData());
    }

    // Iterate through the products in the page.
    private static void printResults(Page<Product> page) {
        for (Product product : page.getData()) {
            System.out.println("Name: " + product.name);
            System.out.println("Description: " + product.description);
            System.out.println("Price: " + product.price);
            System.out.println("--------");
        }
        // Print the `after` cursor to paginate through results.
        System.out.println("After: " + page.getAfter());
    }
}
```


## Connect to Fauna

To send query requests to Fauna, initialize a `FaunaClient` instance with a
Fauna authentication secret. You can pass the secret in a `FaunaConfig` object:

```java
FaunaConfig config = FaunaConfig.builder().secret("FAUNA_SECRET").build();


FaunaClient client = Fauna.client(config);
```

If not specified, `secret` defaults to the `FAUNA_SECRET` environment variable.
For example:

```java
// Defaults to the secret in the `FAUNA_SECRET` env var.
FaunaClient client = Fauna.client();
```

The client comes with a helper config for connecting to Fauna running locally.

```java
// Connects to Fauna running locally via Docker (http://localhost:8443 and secret "secret").
FaunaClient local = Fauna.local();
```


### Scoped client
You can scope a client to a specific database (and role).

```java
FaunaClient db1 = Fauna.scoped(client, FaunaScope.builder("Database1").build());

FaunaScope scope2 = FaunaScope.builder("Database2").withRole(FaunaRole.named("MyRole")).build();
FaunaClient db2 = Fauna.scoped(client, scope2);
```


### Multiple connections

You can use a single client instance to run multiple asynchronous queries at
once. The driver manages HTTP connections as needed. Your app doesn't need to
implement connection pools or other connection management strategies.

You can create multiple client instances to connect to Fauna using different
secrets or client configurations.


## Run FQL queries

Use `fql` templates to compose FQL queries. To run the query, pass the template
and an expected result class to `query()` or `asyncQuery()`:

```java
Query query = fql("Product.sortedByPriceLowToHigh()");
QuerySuccess<Page<Product>> result = client.query(query, pageOf(Product.class));
```

You can also pass a nullable set of [query options](#query-options) to `query()`
or `asyncQuery()`. These options control how the query runs in Fauna. See [Query
options](#query-options).


### Define a custom class for your data

Use annotations to map a Java class to a Fauna document or object shape:

```java
import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaId;

class Person {

    @FaunaId
    private String id;

    private String firstName;

    @FaunaField( name = "dob")
    private String dateOfBirth;
}
```

You can use the `com.fauna.annotation` package to modify encoding and decoding of
specific fields in classes used as arguments and results of queries.
* `@FaunaId`: Should only be used once per class and be associated with a field named `id` that represents the Fauna document ID. It's not encoded unless the `isClientGenerated` flag is `true`.
* `@FaunaTs`: Should only be used once per class and be associated with a field named `ts` that represents the timestamp of a document. It's not encoded.
* `@FaunaColl`: Typically goes unmodeled. Should only be used once per class and be associated with a field named `coll` that represents the collection field of a document. It will never be encoded.
* `@FaunaField`: Can be associated with any field to override its name in Fauna.
* `@FaunaIgnore`: Can be used to ignore fields during encoding and decoding.

Use classes in the `com.fauna.codec` package to handle type erasure when the top-level result
of a query is a generic, including:
* `PageOf<T>` where `T` is the element type.
* `ListOf<T>` where `T` is the element type.
* `MapOf<T>` where `T` is the value type.
* `OptionalOf<T>` where `T` is the value type.
* `NullableDocumentOf<T>` where `T` is the value type. This is specifically for cases when you return a Fauna document that may be null and want to receive a concrete `NullDocument<T>` or `NonNullDocument<T>` instead of catching a `NullDocumentException`.

### Variable interpolation

Use `${}` to pass native Java variables to FQL. You can escape a variable by
prepending an additional `$`.

```java
// Create a native Java var.
var collectionName = "Product";

// Pass the var to an FQL query.
Query query = fql("""
    let collection = Collection(${collectionName})
    collection.sortedByPriceLowToHigh()
    """,
    Map.of(
        "collectionName", collectionName
    ));
```

Passed variables are encoded to an appropriate type and passed to Fauna's HTTP
API. This helps prevent injection attacks.

<!-- TODO: Subqueries -->

## Pagination
Use `paginate()` to asynchronously iterate through sets that contain more than one page of results.

`paginate()` accepts the same [query options](#query-options) as `query()` and `asyncQuery()`.

```java
import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.PageIterator;

public class App {
    public static void main(String[] args) {
        FaunaClient client = Fauna.client();

        // `paginate()` will make an async request to Fauna.
        PageIterator<Product> iter1 = client.paginate(fql("Product.all()"), Product.class);

        // Handle each page. `PageIterator` extends the Java Iterator interface.
        while (iter1.hasNext()) {
            Page<Product> page = iter1.next();
            List<Product> pageData = page.data();
            // Do something with your data.
        }

        PageIterator<Product> iter2 = client.paginate(fql("Product.all()"), Product.class);

        // Use the `flatten()` on PageIterator to iterate over every item in a set.
        Iterator<Product> productIter = iter2.flatten();
        List<Product> products = new ArrayList<>();

        // Iterate over Product elements without worrying about pages.
        iter2.forEachRemaining((Product p) -> products.add(p));
    }
}
```

## Query statistics

Successful query responses and `ServiceException` exceptions include query
statistics:

```java
package org.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.exception.FaunaException;
import com.fauna.exception.ServiceException;
import com.fauna.query.builder.Query;
import static com.fauna.query.builder.Query.fql;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;

public class App {
    public static void main(String[] args) {
        try {
            FaunaClient client = Fauna.client();

            Query query = fql("'Hello world'");

            CompletableFuture<QuerySuccess<String>> futureResponse = client.asyncQuery(query, String.class);

            QueryResponse response = futureResponse.get();

            System.out.println(response.getStats().toString());

        } catch (FaunaException e) {
            if (e instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) e;
                System.out.println(serviceException.getStats().toString());
            }
            System.out.println(e);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
```


## Client configuration

You can pass a `FaunaConfig` object to customize the configuration of a
`FaunaClient` instance.

```java
FaunaConfig config = new FaunaConfig.Builder()
        .secret("FAUNA_SECRET")
        .build();

FaunaClient client = Fauna.client(config);
```


### Environment variables

By default, `secret` and `endpoint` default to the respective `FAUNA_SECRET` and
`FAUNA_ENDPOINT` environment variables.

For example, if you set the following environment variables:

```sh
export FAUNA_SECRET=FAUNA_SECRET
export FAUNA_ENDPOINT=https://db.fauna.com/
```

You can initialize the client with a default configuration:

```java
FaunaClient client = Fauna.client();
```


### Retries

The client automatically retries queries that receive a response with 429 HTTP
status code. The client will retry a query up to 4 times, including the original
query request. Retries use an exponential backoff.


## Query options

You can pass a `QueryOptions` object to `query()` or `asyncQuery()` to control
how a query runs in Fauna. You can also use query options to instrument
a query for monitoring and debugging.

```java
Query query = Query.fql("Hello World");

QueryOptions options = QueryOptions.builder()
    .linearized(true)
    .queryTags(Map.of("tag", "value"))
    .timeout(Duration.ofSeconds(10))
    .traceParent("00-750efa5fb6a131eb2cf4db39f28366cb-000000000000000b-00")
    .typeCheck(false)
    .build();

QuerySuccess result = client.query(query, String.class, options);
```

## Event Feeds (beta)

The driver supports [Event Feeds](https://docs.fauna.com/fauna/current/learn/cdc/#event-feeds).

### Request an Event Feed

An Event Feed asynchronously polls an [event
source](https://docs.fauna.com/fauna/current/learn/cdc/#create-an-event-source)
for paginated events.

To get an event source, append
[`eventSource()`](https://docs.fauna.com/fauna/current/reference/fql-api/schema-entities/set/eventsource/)
or
[`eventsOn()`](https://docs.fauna.com/fauna/current/reference/fql-api/schema-entities/set/eventson/)
to a [supported Set](https://docs.fauna.com/fauna/current/reference/cdc/#sets).

To get an event feed, you can use one of the following methods:

* `feed()`: Synchronously fetches an event feed and returns a `FeedIterator`
   that you can use to iterate through the pages of events.

* `asyncFeed()`: Asynchronously fetches an event feed and returns a
   `CompletableFuture<FeedIterator>` that you can use to iterate through the
   pages of events.

* `poll()`: Asynchronously fetches a single page of events from the event feed
   and returns a `CompletableFuture<FeedPage>` that you can use to handle each
   page individually. You can repeatedly call `poll()` to get successive pages.

You can use `flatten()` on a `FeedIterator` to iterate through events rather
than pages.

```java
import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.event.FeedIterator;
import com.fauna.event.EventSource;
import com.fauna.event.FeedOptions;
import com.fauna.event.FeedPage;
import com.fauna.event.EventSourceResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.event.FaunaEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import static com.fauna.query.builder.Query.fql;

// Import the Product class for event data.
import org.example.Product;

public class EventFeedExample {
    private static void printEventDetails(FaunaEvent<Product> event) {
        System.out.println("Event Details:");
        System.out.println("  Type: " + event.getType());
        System.out.println("  Cursor: " + event.getCursor());

        event.getTimestamp().ifPresent(ts ->
            System.out.println("  Timestamp: " + ts)
        );

        event.getData().ifPresent(product ->
            System.out.println("  Product: " + product.toString())
        );

        if (event.getStats() != null) {
            System.out.println("  Stats: " + event.getStats());
        }

        if (event.getError() != null) {
            System.out.println("  Error: " + event.getError());
        }

        System.out.println("-------------------");
    }

    public static void main(String[] args) {
        FaunaClient client = Fauna.client();

        long tenMinutesAgo = System.currentTimeMillis() * 1000 - (10 * 60 * 1000 * 1000);
        FeedOptions options = FeedOptions.builder()
                .startTs(tenMinutesAgo)
                .pageSize(10)
                .build();

        // Example 1: Using `feed()`
        FeedIterator<Product> syncIterator = client.feed(
            fql("Product.all().eventsOn(.price, .stock)"),
            options,
            Product.class
        );

        System.out.println("----------------------");
        System.out.println("`feed()` results:");
        System.out.println("----------------------");
        syncIterator.forEachRemaining(page -> {
            for (FaunaEvent<Product> event : page.getEvents()) {
                printEventDetails(event);
            }
        });

        // Example 2: Using `asyncFeed()`
        CompletableFuture<FeedIterator<Product>> iteratorFuture = client.asyncFeed(
            fql("Product.all().eventsOn(.price, .stock)"),
            options,
            Product.class
        );

        FeedIterator<Product> iterator = iteratorFuture.join();
        System.out.println("----------------------");
        System.out.println("`asyncFeed()` results:");
        System.out.println("----------------------");
        iterator.forEachRemaining(page -> {
            for (FaunaEvent<Product> event : page.getEvents()) {
                printEventDetails(event);
            }
        });

        // Example 3: Using `flatten()` on a `FeedIterator`
        FeedIterator<Product> flattenedIterator = client.feed(
            fql("Product.all().eventSource()"),
            options,
            Product.class
        );

        Iterator<FaunaEvent<Product>> eventIterator = flattenedIterator.flatten();
        List<FaunaEvent<Product>> allEvents = new ArrayList<>();
        eventIterator.forEachRemaining(allEvents::add);
        System.out.println("----------------------");
        System.out.println("`flatten()` results:");
        System.out.println("----------------------");
        for (FaunaEvent<Product> event : allEvents) {
            printEventDetails(event);
        }

        // Example 4: Using `poll()`
        QuerySuccess<EventSourceResponse> sourceQuery = client.query(
            fql("Product.all().eventSource()"),
            EventSourceResponse.class
        );
        EventSource source = EventSource.fromResponse(sourceQuery.getData());

        CompletableFuture<FeedPage<Product>> pageFuture = client.poll(
            source,
            options,
            Product.class
        );

        while (pageFuture != null) {
            FeedPage<Product> page = pageFuture.join();
            List<FaunaEvent<Product>> events = page.getEvents();

            System.out.println("----------------------");
            System.out.println("`poll()` results:");
            System.out.println("----------------------");
            for (FaunaEvent<Product> event : events) {
                printEventDetails(event);
            }

            if (page.hasNext()) {
                FeedOptions nextPageOptions = options.nextPage(page);
                pageFuture = client.poll(source, nextPageOptions, Product.class);
            } else {
                pageFuture = null;
            }
        }
    }
}
```

If you pass an event source directly to `feed()` or `poll()` and changes occur
between the creation of the event source and the Event Feed request, the feed
replays and emits any related events.

In most cases, you'll get events after a specific start time or cursor.

### Get events after a specific start time

When you first poll an event source using an Event Feed, you usually include a
`startTs` (start timestamp) in the `FeedOptions` passed to `feed()`,
`asyncFeed()`, or `poll()`.

`startTs` is an integer representing a time in microseconds since the Unix
epoch. The request returns events that occurred after the specified timestamp
(exclusive).

```java
Query query = fql("Product.all().eventsOn(.price, .stock)");

// Calculate the timestamp for 10 minutes ago in microseconds.
long tenMinutesAgo = System.currentTimeMillis() * 1000 - (10 * 60 * 1000 * 1000);

FeedOptions options = FeedOptions.builder()
        .startTs(tenMinutesAgo)
        .pageSize(10)
        .build();

// Example 1: Using `feed()`
FeedIterator<Product> syncIterator = client.feed(
    query,
    options,
    Product.class
);

// Example 2: Using `asyncFeed()`
CompletableFuture<FeedIterator<Product>> iteratorFuture = client.asyncFeed(
    query,
    options,
    Product.class
);

// Example 3: Using `poll()`
QuerySuccess<EventSourceResponse> sourceQuery = client.query(
    query,
    EventSourceResponse.class
);
EventSource source = EventSource.fromResponse(sourceQuery.getData());

CompletableFuture<FeedPage<Product>> pageFuture = client.poll(
    source,
    options,
    Product.class
);
```

### Get events after a specific event cursor

After the initial request, you usually get subsequent events using the cursor
for the last page or event. To get events after a cursor (exclusive), include
the cursor in the `FeedOptions` passed to passed to `feed()`,
`asyncFeed()`, or `poll()`.

```java
Query query = fql("Product.all().eventsOn(.price, .stock)");

FeedOptions options = FeedOptions.builder()
        .cursor("gsGabc456") // Cursor for the last page
        .pageSize(10)
        .build();

// Example 1: Using `feed()`
FeedIterator<Product> syncIterator = client.feed(
    query,
    options,
    Product.class
);

// Example 2: Using `asyncFeed()`
CompletableFuture<FeedIterator<Product>> iteratorFuture = client.asyncFeed(
    query,
    options,
    Product.class
);

// Example 3: Using `poll()`
QuerySuccess<EventSourceResponse> sourceQuery = client.query(
    query,
    EventSourceResponse.class
);
EventSource source = EventSource.fromResponse(sourceQuery.getData());

CompletableFuture<FeedPage<Product>> pageFuture = client.poll(
    source,
    options,
    Product.class
);
```

### Error handling

Exceptions can be raised in two different places:

* While fetching a page
* While iterating a page's events

This distinction lets ignore errors originating from event processing. For
example:

```java
try {
    FeedIterator<Product> syncIterator = client.feed(
        fql("Product.all().map(.details.toUpperCase()).eventSource()"),
        options,
        Product.class
    );

    syncIterator.forEachRemaining(page -> {
        try {
            for (FaunaEvent<Product> event : page.getEvents()) {
                // Event-specific handling.
                System.out.println("Event: " + event);
            }
        } catch (FaunaException e) {
            // Handle errors for specific events within the page.
            System.err.println("Error processing event: " + e.getMessage());
        }
    });

} catch (FaunaException e) {
    // Additional handling for initialization errors.
    System.err.println("Error occurred with event feed initialization: " + e.getMessage());
}
```

## Event Streaming

The driver supports [Event
Streaming](https://docs.fauna.com/fauna/current/learn/cdc/#event-streaming).

An Event Stream lets you consume events from an [event
source](https://docs.fauna.com/fauna/current/learn/cdc/#create-an-event-source)
as a real-time subscription.

To get an event source, append
[`eventSource()`](https://docs.fauna.com/fauna/current/reference/reference/schema_entities/set/eventsource)
or
[`eventsOn()`](https://docs.fauna.com/fauna/current/reference/reference/schema_entities/set/eventson)
to a [supported Set](https://docs.fauna.com/fauna/current/reference/cdc/#sets).

To start and subscribe to an Event Stream, use an event source to create a
[`StreamRequest`](https://fauna.github.io/fauna-jvm/latest/com/fauna/stream/StreamRequest.html)
and pass it to  `stream()` or `asyncStream()`:

```java
// Get an event source.
Query query = fql("Product.all().eventSource() { name, stock }");
QuerySuccess<StreamTokenResponse> eventSourceResponse = client.query(query, StreamTokenResponse.class);
String eventSource = eventSourceResponse.getData().getToken();

// Create a StreamRequest.
StreamRequest request = new StreamRequest(eventSource);

// Use stream() when you want to ensure the stream is ready before proceeding
// with other operations, or when working in a synchronous context.
FaunaStream<Product> stream = client.stream(request, Product.class);

// Use asyncStream() when you want to start the stream operation without blocking,
// which is useful in asynchronous applications or when you need to perform other
// tasks while waiting for the stream to be established.
CompletableFuture<FaunaStream<Product>> futureStream = client.asyncStream(request, Product.class);
```

Alternatively, you can pass an FQL query that returns an event source to `stream()` or
`asyncStream()`:

```java
Query query = fql("Product.all().eventSource() { name, stock }");
// Create and subscribe to a stream in one step.
// stream() example:
FaunaStream<Product> stream = client.stream(query, Product.class);
// asyncStream() example:
CompletableFuture<FaunaStream<Product>> futureStream = client.asyncStream(query, Product.class);
```

### Create a subscriber class

The methods return a
[`FaunaStream`](https://fauna.github.io/fauna-jvm/latest/com/fauna/client/FaunaStream.html)
publisher that lets you handle events as they arrive. Create a class with the
`Flow.Subscriber` interface to process events:

```java
package org.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.event.FaunaStream;
import com.fauna.exception.FaunaException;
import static com.fauna.query.builder.Query.fql;
import com.fauna.event.StreamEvent;

// Import the Product class for event data.
import org.example.Product;

public class App {
    public static void main(String[] args) throws InterruptedException {
        try {
            FaunaClient client = Fauna.client();

            // Create a stream of all products. Project the name and stock.
            FaunaStream<Product> stream = client.stream(fql("Product.all().eventSource() { name, stock }"), Product.class);

            // Create a subscriber to handle stream events.
            ProductSubscriber subscriber = new ProductSubscriber();
            stream.subscribe(subscriber);

            // Wait for the subscriber to complete.
            subscriber.awaitCompletion();
        } catch (FaunaException e) {
            System.err.println("Fauna error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class ProductSubscriber implements Flow.Subscriber<StreamEvent<Product>> {
        private final AtomicInteger eventCount = new AtomicInteger(0);
        private Flow.Subscription subscription;
        private final int maxEvents;
        private final CountDownLatch completionLatch = new CountDownLatch(1);

        public ProductSubscriber() {
            // Stream closes after 3 events.
            this.maxEvents = 3;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(StreamEvent<Product> event) {
            // Handle each event...
            int count = eventCount.incrementAndGet();
            System.out.println("Received event " + count + ":");
            System.out.println("  Type: " + event.getType());
            System.out.println("  Cursor: " + event.getCursor());
            System.out.println("  Timestamp: " + event.getTimestamp());
            System.out.println("  Data: " + event.getData().orElse(null));

            if (count >= maxEvents) {
                System.out.println("Closing stream after " + maxEvents + " events");
                subscription.cancel();
                completionLatch.countDown();
            } else {
                subscription.request(1);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println("Error in stream: " + throwable.getMessage());
            completionLatch.countDown();
        }

        @Override
        public void onComplete() {
            System.out.println("Stream completed.");
            completionLatch.countDown();
        }

        public int getEventCount() {
            return eventCount.get();
        }

        public void awaitCompletion() throws InterruptedException {
            completionLatch.await();
        }
    }
}
```

## Debugging / Tracing
If you would like to see the requests and responses the client is making and receiving, you can set the environment
variable `FAUNA_DEBUG=1`. Fauna log the request and response (including headers) to `stderr`. You can also pass in your
own log handler. Setting `Level.WARNING` is equivalent to `FAUNA_DEBUG=0`, while `Level.FINE` is equivalent to
`FAUNA_DEBUG=1`. The client will log the request body at `Level.FINEST`.

```java
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;

class App {
    public static void main(String[] args) {
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new SimpleFormatter());
        FaunaClient client = Fauna.client(FaunaConfig.builder().logHandler(handler).build());
    }
}
```
