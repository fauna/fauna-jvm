package com.fauna.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.exception.InvalidRequestException;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponseInternal;
import com.fauna.response.QuerySuccess;
import com.fauna.codec.PageOf;
import com.fauna.codec.ParameterizedOf;
import com.fauna.types.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
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

    private CompletableFuture<QuerySuccess<PageOf<String>>> successFuture(boolean after, int num) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode page = root.putObject("data");
        if (after) {
            page.put("after", "afterToken");
        }
        ArrayNode arr = page.putArray("data");
        arr.add(num + "-a");
        arr.add(num + "-b");

        var res = MAPPER.readValue(root.asText(), QueryResponseInternal.class);
        QuerySuccess<PageOf<String>> success = new QuerySuccess(DefaultCodecProvider.SINGLETON.get(Page.class, String.class), res);
        return CompletableFuture.supplyAsync(() -> success);
    }

    private CompletableFuture<QuerySuccess<PageOf<String>>> failureFuture() throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "invalid_query");
        var res = MAPPER.readValue(root.asText(), QueryResponseInternal.class);
        return CompletableFuture.failedFuture(new InvalidRequestException(new QueryFailure(400, res)));
    }


    @Test
    public void test_single_page() throws Exception {
        when(client.asyncQuery(any(), any(ParameterizedOf.class), any())).thenReturn(successFuture(false, 0));
        PageIterator<String> pageIterator = new PageIterator<>(client, fql("hello"), String.class, null);
        assertTrue(pageIterator.hasNext());
        assertEquals(pageIterator.next().data(), List.of("0-a", "0-b"));
        assertFalse(pageIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> pageIterator.next());
    }

    @Test
    public void test_single_page_without_calling_hasNext() throws Exception {
        when(client.asyncQuery(any(), any(ParameterizedOf.class), any())).thenReturn(successFuture(false, 0));
        PageIterator<String> pageIterator = new PageIterator<>(client, fql("hello"), String.class, null);
        // No call to hasNext here.
        assertEquals(pageIterator.next().data(), List.of("0-a", "0-b"));
        assertFalse(pageIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> pageIterator.next());
    }

    @Test
    public void test_multiple_pages() throws Exception {
        when(client.asyncQuery(any(), any(ParameterizedOf.class), any())).thenReturn(
                successFuture(true, 0), successFuture(false, 1));
        PageIterator<String> pageIterator = new PageIterator<>(client, fql("hello"), String.class, null);
        assertTrue(pageIterator.hasNext());
        assertEquals(List.of("0-a", "0-b"), pageIterator.next().data());

        assertTrue(pageIterator.hasNext());
        assertEquals(List.of("1-a", "1-b"), pageIterator.next().data());
        assertFalse(pageIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> pageIterator.next());
    }

    @Test
    public void test_error_thrown() throws IOException {
        when(client.asyncQuery(any(), any(ParameterizedOf.class), any())).thenReturn(failureFuture());
        PageIterator<String> pageIterator = new PageIterator<>(client, fql("hello"), String.class, null);
        // We could return the wrapped completion exception here.
        InvalidRequestException exc = assertThrows(InvalidRequestException.class, () -> pageIterator.hasNext());
        assertEquals("invalid_query", exc.getResponse().getErrorCode());
    }

    @Test
    public void test_PageIterator_from_single_Page() {
        Page<String> page = new Page<>(List.of("hello"), null);
        PageIterator<String> pageIterator = new PageIterator<>(client, page, String.class, null);
        assertTrue(pageIterator.hasNext());
        assertEquals(page, pageIterator.next());
        assertFalse(pageIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> pageIterator.next());

    }

    @Test
    public void test_PageIterator_from_Page() throws IOException {
        Page<String> page = new Page<>(List.of("hello"), "foo");
        PageIterator<String> pageIterator = new PageIterator<>(client, page, String.class, null);

        assertTrue(pageIterator.hasNext());
        when(client.asyncQuery(any(), any(ParameterizedOf.class), any())).thenReturn(
                successFuture(false, 0));
        assertEquals(page, pageIterator.next());

        assertTrue(pageIterator.hasNext());
        assertEquals(List.of("0-a", "0-b"), pageIterator.next().data());

        assertFalse(pageIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> pageIterator.next());

    }
}
