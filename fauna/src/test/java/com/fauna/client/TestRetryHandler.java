package com.fauna.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.serialization.Deserializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRetryHandler {

    public static String timestamp() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_TIME);
    }

    public static QuerySuccess successResponse() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode successNode = mapper.createObjectNode();
            return new QuerySuccess<>(Deserializer.DYNAMIC, successNode, null);
        } catch (IOException exc) {
            throw new RuntimeException();
        }
    }

    @Test
    public void testExecute() {
        RetryHandler handler = new RetryHandler(ExponentialBackoffStrategy.builder().build());
        handler.execute(CompletableFuture.supplyAsync(() -> successResponse()));

    }

    @Test
    public void testDefaultHandler() {
        RetryHandler handler = new RetryHandler(ExponentialBackoffStrategy.builder().build());
        assertEquals(750, handler.getDelayMillis(), 250);
        assertEquals(1500, handler.getDelayMillis(), 500);
    }

    @Test
    public void testDelay() throws ExecutionException, InterruptedException {
        //this.sendAsync(timestamp()).thenCompose( CompletableFuture.supplyAsync(() -> successResponse()));
    }
}
