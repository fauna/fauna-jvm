package com.fauna.perf;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.perf.model.Product;
import com.fauna.perf.testdata.TestDataParser;
import com.fauna.query.AfterToken;
import com.fauna.query.builder.Query;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.fauna.codec.Generic.pageOf;
import static com.fauna.query.builder.Query.fql;

public class PerformanceTest {
    private static FaunaClient client;

    private static Stream<Arguments> getTestData() throws IOException {
        return TestDataParser.getQueriesFromFile();
    }

    @BeforeAll
    public static void setup() {
        client = Fauna.client();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        MetricsHandler.writeMetricsToFile();
    }

    @ParameterizedTest
    @MethodSource("getTestData")
    @Tag("perfTests")
    public void executeQueryAndCollectStats(String name,
                                            List<String> queryParts,
                                            boolean typed, boolean page)
            throws InterruptedException, ExecutionException {
        if (queryParts.size() == 0) {
            System.out.println("Skipping empty query from queries.json");
            return;
        }

        for (int i = 0; i < 20; i++) {
            Query query = getCompositedQueryFromParts(queryParts);
            AtomicInteger queryTime = new AtomicInteger(0);

            long startTime = System.currentTimeMillis();

            CompletableFuture<Void> future = null;

            if (typed && page) {
                var result =
                        client.asyncQuery(query, pageOf(Product.class)).get();
                int queryCount = 1;
                int queryTimeAgg = result.getStats().queryTimeMs;

                while (result.getData().getAfter().isPresent()) {
                    AfterToken after = result.getData().getAfter().get();
                    result = client.asyncQuery(
                            fql("Set.paginate(${after})",
                                    Map.of("after", after.getToken())),
                            pageOf(Product.class)).get();
                    queryCount++;
                    queryTimeAgg += result.getStats().queryTimeMs;
                }

                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                MetricsHandler.recordMetrics(
                        name + " (query)",
                        (int) elapsedTime / queryCount,
                        queryTimeAgg / queryCount);
            } else if (typed) {
                future = client.asyncQuery(query, Product.class)
                        .thenAccept(result -> {
                            queryTime.set(result.getStats().queryTimeMs);
                        });
            } else {
                future = client.asyncQuery(query)
                        .thenAccept(result -> {
                            queryTime.set(result.getStats().queryTimeMs);
                        });
            }

            if (!page) {
                future.thenRun(() -> {
                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    MetricsHandler.recordMetrics(name, (int) elapsedTime,
                            queryTime.get());
                }).get();
            }
        }
    }

    private Query getCompositedQueryFromParts(List<String> parts) {
        if (parts.size() == 1) {
            return fql(parts.get(0));
        }

        return fql(String.join("", parts));
    }
}
