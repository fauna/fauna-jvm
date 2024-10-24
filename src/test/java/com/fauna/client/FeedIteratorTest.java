package com.fauna.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.InvalidRequestException;
import com.fauna.feed.FeedRequest;
import com.fauna.feed.FeedSuccess;
import com.fauna.response.ErrorInfo;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import com.fauna.response.StreamEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class FeedIteratorTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private FaunaClient client;

    private CompletableFuture<FeedSuccess<String>> successFuture(boolean after, int num) throws IOException {
        List<StreamEvent<String>> events = new ArrayList<>();
        Codec<String> codec = DefaultCodecProvider.SINGLETON.get(String.class);
        events.add(new StreamEvent<>(StreamEvent.EventType.ADD,
                "cursor0", System.currentTimeMillis() - 10,
                num + "-a", null, null));
        events.add(new StreamEvent<>(StreamEvent.EventType.ADD,
                "cursor0", System.currentTimeMillis() - 5,
                num + "-b", null, null));

        return CompletableFuture.supplyAsync(() -> FeedSuccess.builder(codec).events(events).cursor("cursor0").hasNext(after).build());
    }

    private CompletableFuture<FeedSuccess<String>> failureFuture() throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "invalid_query");

        QueryFailure failure = new QueryFailure(400, QueryResponse.builder(null).error(ErrorInfo.builder().code("invalid_query").build()));
        return CompletableFuture.failedFuture(new InvalidRequestException(failure));
    }


    @Test
    public void test_single_page() throws IOException {
        FeedRequest req = FeedRequest.builder("token").pageSize(8).build();
        when(client.asyncFeed(req, String.class)).thenReturn(successFuture(false, 0));
        FeedIterator<String> feedIterator = new FeedIterator<>(client, req, String.class);
        assertTrue(feedIterator.hasNext());
        assertEquals(List.of("0-a", "0-b"), feedIterator.next().getEvents().stream().map(e -> e.getData().get()).collect(Collectors.toList()));
        assertFalse(feedIterator.hasNext());
        assertThrows(NoSuchElementException.class, feedIterator::next);
    }

    @Test
    public void test_single_page_without_calling_hasNext() throws IOException {
        FeedRequest req = FeedRequest.builder("token").build();
        when(client.asyncFeed(req, String.class)).thenReturn(successFuture(false, 0));
        FeedIterator<String> feedIterator = new FeedIterator<>(client, req, String.class);
        // No call to hasNext here.
        assertEquals(List.of("0-a", "0-b"), feedIterator.next().getEvents().stream().map(e -> e.getData().get()).collect(Collectors.toList()));
        assertFalse(feedIterator.hasNext());
        assertThrows(NoSuchElementException.class, feedIterator::next);
    }

    @Test
    public void test_multiple_pages() throws IOException {
        FeedRequest req = FeedRequest.builder("token").build();
        when(client.asyncFeed(any(FeedRequest.class), eq(String.class))).thenReturn(successFuture(true, 0), successFuture(false, 1));
        FeedIterator<String> feedIterator = new FeedIterator<>(client, req, String.class);

        assertTrue(feedIterator.hasNext());
        assertEquals(List.of("0-a", "0-b"), feedIterator.next().getEvents().stream().map(e -> e.getData().get()).collect(Collectors.toList()));
        assertTrue(feedIterator.hasNext());

        assertEquals(List.of("1-a", "1-b"), feedIterator.next().getEvents().stream().map(e -> e.getData().get()).collect(Collectors.toList()));
        assertFalse(feedIterator.hasNext());
        assertThrows(NoSuchElementException.class, feedIterator::next);
    }

    @Test
    public void test_multiple_pages_async() throws IOException, ExecutionException, InterruptedException {
        FeedRequest req = FeedRequest.builder("token").build();
        when(client.asyncFeed(any(FeedRequest.class), eq(String.class))).thenReturn(successFuture(true, 0), successFuture(false, 1));
        FeedIterator<String> feedIterator = new FeedIterator<>(client, req, String.class);

        boolean hasNext = feedIterator.hasNext();
        List<String> products = new ArrayList<>();
        while (hasNext) {
            hasNext = feedIterator.nextAsync().thenApply(page -> {
                products.addAll(page.getEvents().stream().map(e -> e.getData().get()).collect(Collectors.toList()));
                return feedIterator.hasNext(); }).get();
        }
        assertEquals(4, products.size());
    }

    @Test
    public void test_flatten() throws IOException  {
        when(client.asyncFeed(any(FeedRequest.class), eq(String.class))).thenReturn(successFuture(true, 0), successFuture(false, 1));
        FeedRequest req = FeedRequest.builder("token").build();
        FeedIterator<String> feedIterator = new FeedIterator<>(client, req, String.class);
        Iterator<StreamEvent<String>> iter = feedIterator.flatten();
        List<String> products = new ArrayList<>();
        iter.forEachRemaining(event -> products.add(event.getData().orElseThrow()));
        assertEquals(4, products.size());

    }

    @Test
    public void test_error_thrown() throws IOException {
        FeedRequest req = FeedRequest.builder("token").build();
        when(client.asyncFeed(req, String.class)).thenReturn(failureFuture());
        FeedIterator<String> feedIterator = new FeedIterator<>(client, req, String.class);

        // We could return the wrapped completion exception here.
        assertTrue(feedIterator.hasNext());
        InvalidRequestException exc = assertThrows(InvalidRequestException.class, () -> feedIterator.next());
        assertEquals("invalid_query", exc.getResponse().getErrorCode());
    }
}
