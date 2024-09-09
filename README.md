# The Official JVM Driver for [Fauna](https://fauna.com) (alpha)

> [!CAUTION]
> This driver is currently in alpha and should not be used in production.

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

import static com.fauna.codec.generic.pageOf;
import static com.fauna.query.builder.Query.fql;


public class App {

    // Define class for `Product` documents
    // in expected results.
    public static class Product {
        public String name;

        public String description;

        public double price;
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
        for (Product product : page.data()) {
            System.out.println("Name: " + product.name);
            System.out.println("Description: " + product.description);
            System.out.println("Price: " + product.price);
            System.out.println("--------");
        }
        // Print the `after` cursor to paginate through results.
        System.out.println("After: " + page.after());
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
Query query = Query.fql("Product.sortedByPriceLowToHigh()");
QuerySuccess<Page<Product>> result = client.query(query, new PageOf<>(Product.class));
```

You can also pass a nullable set of [query options](#query-options) to `query()`
or `asyncQuery()`. These options control how the query runs in Fauna. See [Query
options](#query-options).


### Define a custom class for your data
These should be simple POJOs.

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
* `@FaunaId`: Should only be used once per class and be associated with a field named `id` if it represents the ID of a document. It will not be encoded unless the `isClientGenerated` flag is set.
* `@FaunaTs`: Should only be used once per class and be associated with a field named `ts` if it represents the timestamp of a document. It will never be encoded.
* `@FaunaColl`: Typically goes unmodeled. Should only be used once per class and be associated with a field named `coll` if represents the collection field of a document. It will never be encoded.
* `@FaunaField`: Can be associated with any field to override its name in Fauna.
* `@FaunaIgnore`: Can be used to ignore fields during encoding and decoding.

In the `com.fauna.codec` package, you use classes to handle type erasure when the top-level result
of a query is a generic, including:
* `PageOf<T>` where `T` is the element type.
* `ListOf<T>` where `T` is the element type.
* `MapOf<T>` where `T` is the value type.
* `OptionalOf<T>` where `T` is the value type.
* `NullableOf<T>` where `T` is the value type. This is specifically for cases when returning a Fauna Document that may be null and you want to receive a concrete NullDoc<T> or NonNullDoc<T> instead of catching a NullDocumentException.

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
import com.fauna.client.FaunaConfig;
import com.fauna.exception.FaunaException;
import com.fauna.exception.ServiceException;
import com.fauna.query.builder.Query;
import static com.fauna.query.builder.Query.fql;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;

public class App {
    public static void main(String[] args) {
        try {
            FaunaConfig config = FaunaConfig.builder().secret("FAUNA_SECRET").build();
            FaunaClient client = Fauna.client(config);

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

## Event streaming

The driver supports [event streaming](https://docs.fauna.com/fauna/current/learn/streaming).

To get a stream token, append
[`toStream()`](https://docs.fauna.com/fauna/current/reference/reference/schema_entities/set/tostream)
or
[`changesOn()`](https://docs.fauna.com/fauna/current/reference/reference/schema_entities/set/changeson)
to a set from a [supported
source](https://docs.fauna.com/fauna/current/reference/streaming_reference/#supported-sources

To start and subscribe to the stream, use a stream token to create a
`StreamRequest` and pass the `StreamRequest` to  `stream()` or `asyncStream()`:

```java
// Get a stream token.
Query query = fql("Product.all().toStream() { name, stock }");
QuerySuccess<StreamTokenResponse> tokenResponse = client.query(query, StreamTokenResponse.class);
String streamToken = tokenResponse.getData().getToken();

// Create a StreamRequest.
StreamRequest request = new StreamRequest(streamToken);

// Use stream() when you want to ensure the stream is ready before proceeding
// with other operations, or when working in a synchronous context.
FaunaStream<Product> stream = client.stream(request, Product.class);

// Use asyncStream() when you want to start the stream operation without blocking,
// which is useful in asynchronous applications or when you need to perform other
// tasks while waiting for the stream to be established.
CompletableFuture<FaunaStream<Product>> futureStream = client.asyncStream(request, Product.class);
```

Alternatively, you also pass an FQL that returns a stream token to `stream()` or
`asyncStream()`:

```java
Query query = fql("Product.all().toStream() { name, stock }");
// Create and subscribe to a stream in one step.
// stream() example:
FaunaStream<Product> stream = client.stream(query, Product.class);
// asyncStream() example:
CompletableFuture<FaunaStream<Product>> futureStream = client.asyncStream(query, Product.class);
```

### Create a subscriber class

The methods return a `FaunaStream` publisher that lets you handle events as they
arrive. Create a class with the `Flow.Subscriber` interface to process
events:

```java
package org.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaStream;
import com.fauna.exception.FaunaException;
import static com.fauna.query.builder.Query.fql;
import com.fauna.response.StreamEvent;

// Import the Product class for event data.
import org.example.Product;

public class App {
    public static void main(String[] args) throws InterruptedException {
        try {
            FaunaClient client = Fauna.client();

            // Create a stream of all products. Project the name and stock.
            FaunaStream<Product> stream = client.stream(fql("Product.all().toStream() { name, stock }"), Product.class);

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
