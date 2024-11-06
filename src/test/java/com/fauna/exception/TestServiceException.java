package com.fauna.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fauna.response.ErrorInfo;
import com.fauna.response.QueryFailure;
import com.fauna.response.QueryResponse;
import com.fauna.response.QueryStats;
import com.fauna.query.QueryTags;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestServiceException {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testNullResponseThrowsNullPointer() {
        assertThrows(NullPointerException.class,
                () -> new ServiceException(null));
    }

    @Test
    public void testGetters() throws IOException {
        // Given
        QueryFailure failure = new QueryFailure(500,
                QueryResponse.builder(null).summary("summarized")
                        .schemaVersion(10L)
                        .stats(new QueryStats(100, 0, 0, 0, 0, 0, 0, 0, null))
                        .queryTags(QueryTags.of("foo=bar"))
                        .lastSeenTxn(Long.MAX_VALUE / 4).error(
                                ErrorInfo.builder().code("bad_thing")
                                        .message("message in a bottle").build()));

        // When
        ServiceException exc = new ServiceException(failure);

        // Then
        assertEquals(500, exc.getStatusCode());
        assertEquals("bad_thing", exc.getErrorCode());
        assertEquals("500 (bad_thing): message in a bottle\n---\nsummarized",
                exc.getMessage());
        assertEquals("summarized", exc.getSummary());
        assertEquals(100, exc.getStats().computeOps);
        assertEquals(10, exc.getSchemaVersion());
        assertEquals(Optional.of(Long.MAX_VALUE / 4), exc.getTxnTs());
        assertEquals(Map.of("foo", "bar"), exc.getQueryTags());

    }
}
