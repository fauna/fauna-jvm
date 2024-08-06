package com.fauna.client;

import com.fauna.response.QuerySuccess;
import com.fauna.serialization.generic.PageOf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.fauna.query.builder.Query.fql;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PageIteratorTest {

    @Mock
    private FaunaClient client;


    @Test
    public void testPageIterator() throws Exception {
        PageIterator<String> pageIterator = new PageIterator<>(client, fql("hello"), String.class, null);
        QuerySuccess success = new QuerySuccess()
        when(client.query(any(), PageOf.class, any())).thenReturn(QuerySuccess<String>.);
    }


}
