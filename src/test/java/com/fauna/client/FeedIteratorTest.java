package com.fauna.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.event.FeedIterator;
import com.fauna.exception.InvalidRequestException;
import com.fauna.event.EventSource;
import com.fauna.event.FeedOptions;
import com.fauna.event.FeedPage;
import com.fauna.response.ErrorInfo;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import com.fauna.event.StreamEvent;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class FeedIteratorTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final EventSource source = EventSource.fromToken("token");
    private static final String CURSOR_0 = "cursor0";

    @Mock
    private FaunaClient client;

    private CompletableFuture<FeedPage<String>> successFuture(boolean after, int num) throws IOException {
        List<StreamEvent<String>> events = new ArrayList<>();
        Codec<String> codec = DefaultCodecProvider.SINGLETON.get(String.class);
        events.add(new StreamEvent<>(StreamEvent.EventType.ADD,
                "cursor0", System.currentTimeMillis() - 10,
                num + "-a", null, null));
        events.add(new StreamEvent<>(StreamEvent.EventType.ADD,
                "cursor0", System.currentTimeMillis() - 5,
                num + "-b", null, null));

        return CompletableFuture.supplyAsync(() -> FeedPage.builder(codec).events(events).cursor("cursor0").hasNext(after).build());
    }

    private CompletableFuture<FeedPage<String>> failureFuture() throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "invalid_query");

        QueryFailure failure = new QueryFailure(400, QueryResponse.builder(null).error(ErrorInfo.builder().code("invalid_query").build()));
        return CompletableFuture.failedFuture(new InvalidRequestException(failure));
    }


    @Test
    public void test_single_page() throws IOException {
        FeedOptions options = FeedOptions.builder().pageSize(8).build();
        when(client.poll(source, options, String.class)).thenReturn(successFuture(false, 0));
        FeedIterator<String> feedIterator = new FeedIterator<>(client, source, options, String.class);
        assertTrue(feedIterator.hasNext());
        assertEquals(List.of("0-a", "0-b"), feedIterator.next().getEvents().stream().map(e -> e.getData().get()).collect(Collectors.toList()));
        assertFalse(feedIterator.hasNext());
        assertThrows(NoSuchElementException.class, feedIterator::next);
    }

    @Test
    public void test_single_page_without_calling_hasNext() throws IOException {
        when(client.poll(source, FeedOptions.DEFAULT, String.class)).thenReturn(successFuture(false, 0));
        FeedIterator<String> feedIterator = new FeedIterator<>(client, source, FeedOptions.DEFAULT, String.class);
        // No call to hasNext here.
        assertEquals(List.of("0-a", "0-b"), feedIterator.next().getEvents().stream().map(e -> e.getData().get()).collect(Collectors.toList()));
        assertFalse(feedIterator.hasNext());
        assertThrows(NoSuchElementException.class, feedIterator::next);
    }

    @Test
    public void test_multiple_pages() throws IOException {
        when(client.poll(argThat(source::equals), argThat(FeedOptions.DEFAULT::equals), any(Class.class))).thenReturn(successFuture(true, 0));
        when(client.poll(argThat(source::equals), argThat(opts -> opts.getCursor().orElse("").equals(CURSOR_0)), any(Class.class))).thenReturn(successFuture(false, 1));
        FeedIterator<String> feedIterator = new FeedIterator<>(client, source, FeedOptions.DEFAULT, String.class);

        assertTrue(feedIterator.hasNext());
        assertEquals(List.of("0-a", "0-b"), feedIterator.next().getEvents().stream().map(e -> e.getData().get()).collect(Collectors.toList()));
        assertTrue(feedIterator.hasNext());

        assertEquals(List.of("1-a", "1-b"), feedIterator.next().getEvents().stream().map(e -> e.getData().get()).collect(Collectors.toList()));
        assertFalse(feedIterator.hasNext());
        assertThrows(NoSuchElementException.class, feedIterator::next);
    }

    @Test
    public void test_multiple_pages_async() throws IOException, ExecutionException, InterruptedException {
        when(client.poll(argThat(source::equals), any(), any(Class.class))).thenReturn(successFuture(true, 0), successFuture(false, 1));
        FeedIterator<String> feedIterator = new FeedIterator<>(client, source, FeedOptions.DEFAULT, String.class);

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
        when(client.poll(argThat(source::equals), argThat(FeedOptions.DEFAULT::equals), any(Class.class))).thenReturn(successFuture(true, 0));
        when(client.poll(argThat(source::equals), argThat(opts -> opts.getCursor().orElse("").equals(CURSOR_0)), any(Class.class))).thenReturn(successFuture(false, 1));
        FeedIterator<String> feedIterator = new FeedIterator<>(client, source, FeedOptions.DEFAULT, String.class);
        Iterator<StreamEvent<String>> iter = feedIterator.flatten();
        List<String> products = new ArrayList<>();
        iter.forEachRemaining(event -> products.add(event.getData().orElseThrow()));
        assertEquals(4, products.size());

    }

    @Test
    public void test_error_thrown() throws IOException {
        when(client.poll(source, FeedOptions.DEFAULT, String.class)).thenReturn(failureFuture());
        FeedIterator<String> feedIterator = new FeedIterator<>(client, source, FeedOptions.DEFAULT, String.class);

        // We could return the wrapped completion exception here.
        assertTrue(feedIterator.hasNext());
        InvalidRequestException exc = assertThrows(InvalidRequestException.class, () -> feedIterator.next());
        assertEquals("invalid_query", exc.getResponse().getErrorCode());
    }
}
