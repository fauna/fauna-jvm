package com.fauna.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.PageOf;
import com.fauna.codec.ParameterizedOf;
import com.fauna.exception.InvalidRequestException;
import com.fauna.response.ErrorInfo;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import com.fauna.response.QuerySuccess;
import com.fauna.types.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PageIteratorTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private FaunaClient client;

    private CompletableFuture<QuerySuccess<PageOf<Object>>> successFuture(
            boolean after, int num) throws IOException {
        ObjectNode page = MAPPER.createObjectNode();
        if (after) {
            page.put("after", "afterToken");
        }
        ArrayNode arr = page.putArray("data");
        arr.add(num + "-a");
        arr.add(num + "-b");

        QueryResponse.Builder builder = QueryResponse.builder(
                        DefaultCodecProvider.SINGLETON.get(Page.class,
                                new Type[] {String.class}))
                .data(MAPPER.createParser(page.toString()));
        return CompletableFuture.supplyAsync(() -> new QuerySuccess<>(builder));
    }

    private CompletableFuture<QuerySuccess<Object>> failureFuture()
            throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "invalid_query");

        QueryFailure failure = new QueryFailure(400, QueryResponse.builder(null)
                .error(ErrorInfo.builder().code("invalid_query").build()));
        return CompletableFuture.failedFuture(
                new InvalidRequestException(failure));
    }


    @Test
    public void test_single_page() throws Exception {
        when(client.asyncQuery(any(), any(ParameterizedOf.class),
                any())).thenReturn(successFuture(false, 0));
        PageIterator<String> pageIterator =
                new PageIterator<>(client, fql("hello"), String.class, null);
        assertTrue(pageIterator.hasNext());
        assertEquals(pageIterator.next().getData(), List.of("0-a", "0-b"));
        assertFalse(pageIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> pageIterator.next());
    }

    @Test
    public void test_single_page_without_calling_hasNext() throws Exception {
        when(client.asyncQuery(any(), any(ParameterizedOf.class),
                any())).thenReturn(successFuture(false, 0));
        PageIterator<String> pageIterator =
                new PageIterator<>(client, fql("hello"), String.class, null);
        // No call to hasNext here.
        assertEquals(pageIterator.next().getData(), List.of("0-a", "0-b"));
        assertFalse(pageIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> pageIterator.next());
    }

    @Test
    public void test_multiple_pages() throws Exception {
        when(client.asyncQuery(any(), any(ParameterizedOf.class),
                any())).thenReturn(
                successFuture(true, 0), successFuture(false, 1));
        PageIterator<String> pageIterator =
                new PageIterator<>(client, fql("hello"), String.class, null);
        assertTrue(pageIterator.hasNext());
        assertEquals(List.of("0-a", "0-b"), pageIterator.next().getData());

        assertTrue(pageIterator.hasNext());
        assertEquals(List.of("1-a", "1-b"), pageIterator.next().getData());
        assertFalse(pageIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> pageIterator.next());
    }

    @Test
    public void test_multiple_pages_async() throws Exception {
        when(client.asyncQuery(any(), any(ParameterizedOf.class),
                any())).thenReturn(
                successFuture(true, 0), successFuture(false, 1));
        PageIterator<String> pageIterator =
                new PageIterator<>(client, fql("hello"), String.class, null);

        boolean hasNext = pageIterator.hasNext();
        List<String> products = new ArrayList<>();
        while (hasNext) {
            hasNext = pageIterator.nextAsync().thenApply(page -> {
                products.addAll(page.getData());
                return pageIterator.hasNext();
            }).get();
        }
        assertEquals(4, products.size());
    }

    @Test
    public void test_error_thrown() throws IOException {
        when(client.asyncQuery(any(), any(ParameterizedOf.class),
                any())).thenReturn(failureFuture());
        PageIterator<String> pageIterator =
                new PageIterator<>(client, fql("hello"), String.class, null);
        // We could return the wrapped completion exception here.
        assertTrue(pageIterator.hasNext());
        InvalidRequestException exc =
                assertThrows(InvalidRequestException.class,
                        () -> pageIterator.next());
        assertEquals("invalid_query", exc.getResponse().getErrorCode());
    }
}
