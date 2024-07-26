# The Official Java Driver for [Fauna](https://fauna.com).

The Fauna Java driver is a lightweight, open-source wrapper for Fauna's [HTTP
API](https://docs.fauna.com/fauna/current/reference/http/reference/). You can
use the driver to run FQL queries and get results from a Java application.

See the [Fauna docs](https://docs.fauna.com/fauna/current/) for
additional information on how to configure and query your databases.

> [!IMPORTANT]
> This driver can only be used with FQL v10 and is not compatible with earlier
> versions of FQL. To query your databases with earlier API versions, use the
> [faunadb-jvm](https://github.com/fauna/faunadb-jvm) driver.


## Requirements

- Java 11 or later

<!-- TODO: ## Javadocs

Link to Javadocs here  -->

## Installation

The driver is available on the Maven central repository.
You can add the driver to your Java project using Gradle or Maven.

### Gradle

<!-- TODO: Verify artifacts -->

File `build.gradle`:
```
dependencies {
    ...
    implementation "com.fauna:fauna-jvm-1.0.0"
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
      <version>1.0.0</version>
    </dependency>
    ...
</dependencies>
```


## Basic usage

The following application:

* Initializes a client instance to connect to Fauna.
* Composes a basic FQL query using an FQL template.
* Runs the query using `query()`  and `asyncQuery()`.

```java
package org.example;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;
import com.fauna.exception.FaunaException;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.types.Page;

public class App {
    public static void main(String[] args) {
        try {
            // Configure the Fauna client.
            var config = new FaunaConfig.Builder()
                    .secret("FAUNA_SECRET")
                    .build();

            // Initialize the client.
            var client = new FaunaClient(config);

            // Compose a query.
            var query = Query.fql("""
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
        }
    }

    // Use `query()` to run a synchronous query.
    // Synchronous queries block the current thread until the query completes.
    // Returns a `QueryResponse`.
    private static void runSynchronousQuery(FaunaClient client, Query query) {
        var res = client.query(query);
        if (res instanceof QueryFailure) {
            throw new IllegalStateException("Query failed with: " + ((QueryFailure) res).getMessage());
        }

        var success = (QuerySuccess<Page<Map<String, Object>>>) res;
        printResults(success.getData());
    }

    // Use `asyncQuery()` to run an asynchronous, non-blocking query.
    // Returns a `CompletableFuture<QueryResponse>`.
    private static void runAsynchronousQuery(FaunaClient client, Query query) {
        CompletableFuture<QueryResponse> futureResult = client.asyncQuery(query);

        futureResult.thenAccept(res -> {
            if (res instanceof QueryFailure) {
                throw new IllegalStateException("Query failed with: " + ((QueryFailure) res).getMessage());
            }

            var success = (QuerySuccess<Page<Map<String, Object>>>) res;
            printResults(success.getData());
        }).join();
    }

    // Iterate through the products in the page.
    private static void printResults(Page<Map<String, Object>> page) {
        for (Map<String, Object> product : page.data()) {
            System.out.println("Name: " + product.get("name"));
            System.out.println("Description: " + product.get("description"));
            System.out.println("Price: " + product.get("price"));
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
var config = new FaunaConfig.Builder()
        .secret("FAUNA_SECRET")
        .build();

var client = new FaunaClient(config);
```

If not specified, `secret` defaults to the `FAUNA_SECRET` environment variable.
For example:

```java
// Defaults to the secret in the `FAUNA_SECRET` env var.
var client = new FaunaClient();
```


### Multiple connections

You can use a single client instance to run multiple asynchronous queries at
once. The driver manages HTTP connections as needed. Your app doesn't need to
implement connection pools or other connection management strategies.

You can create multiple client instances to connect to Fauna using different
secrets or client configurations.


## Run FQL queries

Use `fql` templates to compose FQL queries. Run the queries using `query()`
or `asyncQuery()`:

```java
var query = Query.fql("Product.sortedByPriceLowToHigh()");
client.asyncQuery(query);
```

You can pass [query options](#query-options) to `query()` or `asyncQuery()` to control how
the query runs in Fauna. See [Query options](#query-options).


### Variable interpolation

Use `${}` to pass native Java variables to FQL. You can escape a variable by
prepending an additional `$`.

```java
// Create a native Java var.
var collectionName = "Product";

// Pass the var to an FQL query.
var query = Query.fql("""
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

## Query statistics

Successful query responses and `ServiceException` exceptions include query
statistics:

```java
package org.example;

import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;
import com.fauna.exception.FaunaException;
import com.fauna.exception.ServiceException;
import com.fauna.query.builder.Query;
import com.fauna.response.QueryResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class App {
    public static void main(String[] args)
 {
        try {
            var config = new FaunaConfig.Builder()
                    .secret("FAUNA_SECRET")
                    .build();
            var client = new FaunaClient(config);

            var query = Query.fql("'Hello world'");

            CompletableFuture<QueryResponse> futureResponse = client.asyncQuery(query);

            QueryResponse response = futureResponse.get();

            System.out.println(response.getStats().toString());

        } catch (FaunaException e) {
            if (e instanceof ServiceException serviceException) {
                System.out.println(serviceException.getStats().toString());
                System.out.println(e);
            } else {
                System.out.println(e);
            }
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
var config = new FaunaConfig.Builder()
        .endpoint("https://db.us.fauna.com")
        .secret("FAUNA_SECRET")
        .build();

var client = new FaunaClient(config);
```

The following table outlines properties of the `FaunaConfig` class and their
defaults.

| Property   | Type   | Required | Description                                                                                                                                                                       |
| ---------- | ------ | -------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `secret`   | String |          | Fauna authentication secret used to authorize requests. Defaults to the `FAUNA_SECRET` environment variable.                                                                      |
| `endpoint` | String |          | Base URL used by the driver for Fauna HTTP API requests. Defaults to the `FAUNA_ENDPOINT` environment variable. If `FAUNA_ENDPOINT` is not set, defaults to https://db.fauna.com. |


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
var client = new FaunaClient();
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
var query = Query.fql("Hello World");

var options = QueryOptions.builder()
    .linearized(true)
    .queryTags(Map.of("name", "hello world query"))
    .timeout(Duration.ofSeconds(10))
    .traceParent("00-750efa5fb6a131eb2cf4db39f28366cb-000000000000000b-00")
    .typeCheck(false)
    .build();

client.query(query, options)
```

The following table outlines properties of the `QueryOptions` class and their
defaults.

| Property      | Type                     | Required | Description                                                                                                                                                                                                                                                                                                                                                                                                        |
| ------------- | ------------------------ | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `linearize`   | Boolean                  |          | If `true`, the query is linearized, ensuring strict serialization of reads and writes. Defaults to `null` (false).<br><p>Maps to the [`x-linearized`](https://docs.fauna.com/fauna/current/reference/http/reference/query/post/#header) HTTP header.</p>                                                                                                                                                                |
| `queryTags`   | `<Map<String, String>> ` |          | Key-value tags used to identify the query. Defaults to `null` (none). Query tags are included in [query logs](https://docs.fauna.com/fauna/current/tools/query-logs/reference/schema/) and the response body for successful queries. The tags are typically used for monitoring.<br><p>Maps to the [`x-query-tags`](https://docs.fauna.com/fauna/current/reference/http/reference/query/post/#header) HTTP header.</p> |
| `timeout`     | Duration                 |          | Maximum amount of time Fauna runs the query before marking it as failed. Defaults to 5 seconds. Maps to the [`x-query-timeout-ms`](https://docs.fauna.com/fauna/current/reference/http/reference/query/post/#header) HTTP header.                                                                                                                                                                                  |
| `traceParent` | String                   |          | W3C-compliant traceparent ID for the request. Defaults to `null` (none). <br><p>If you omit the traceparent ID or provide an invalid ID, Fauna generates a valid one. The traceparent ID is included in query logs. Traceparent IDs are typically used for monitoring.</p>                                                                                                                                             |
| `typeCheck`   | Boolean                  |          | If `true`, enables type checking for the query. Defaults to the database's type checking setting.<br><p>If `true`, type checking must be enabled on the database. Maps to the [`x-typecheck`](https://docs.fauna.com/fauna/current/reference/http/reference/query/post/#header) HTTP header.</p>                                                                                                                       |
