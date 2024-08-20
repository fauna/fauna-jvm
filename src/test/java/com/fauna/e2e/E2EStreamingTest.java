package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.StreamIterator;
import com.fauna.e2e.beans.Author;
import com.fauna.response.StreamEvent;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static com.fauna.query.builder.Query.fql;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class E2EStreamingTest {
    public static final FaunaClient client = Fauna.local();

    @BeforeAll
    public static void setup() {
        Fixtures.PersonCollection(client);
    }

    @Test
    @Ignore
    public void query_streamOfPerson() throws InterruptedException {
        StreamIterator iter = client.stream(fql("Author.all().toStream()"), Author.class);
        assertFalse(iter.hasNext());
        Thread.sleep(1000);
        assertTrue(iter.hasNext());
        StreamEvent a = iter.next();
        assertFalse(iter.hasNext());
        assertThrows(NoSuchElementException.class, () -> iter.next());

    }
}
