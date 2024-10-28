package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.FaunaConfig;
import com.fauna.client.PageIterator;
import com.fauna.e2e.beans.Product;
import com.fauna.response.QuerySuccess;
import com.fauna.codec.PageOf;
import com.fauna.types.Document;
import com.fauna.types.Page;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class E2EPaginationTest {

    public static final FaunaClient client = Fauna.local();

    @BeforeAll
    public static void setup() {
        Fixtures.ProductCollection(client);
    }

    @Test
    public void query_single_item_gets_wrapped_in_page() {
        PageIterator<Product> iter = client.paginate(fql("Product.firstWhere(.name == 'product-1')"), Product.class);
        assertTrue(iter.hasNext());
        Page<Product> page = iter.next();
        assertEquals(1, page.getData().size());
        assertTrue(page.getAfter().isEmpty());
        assertFalse(iter.hasNext());
        assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    public void query_single_object_gets_wrapped_in_page() {
        PageIterator<Object> iter = client.paginate(fql("Product.firstWhere(.name == 'product-1')"));
        assertTrue(iter.hasNext());
        // We didn't pass in a type, so the client returns Page<Object>
        Page<Object> page = iter.next();
        assertEquals(1, page.getData().size());
        // In this case the "Object" is actually a Document, so we can cast it.
        Document document = (Document) page.getData().get(0);
        assertEquals("product-1", document.get("name"));
        assertTrue(page.getAfter().isEmpty());
        assertFalse(iter.hasNext());
        assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    public void query_single_page_gets_wrapped_in_page() {
        PageIterator<Product> iter = client.paginate(fql("Product.where(.quantity < 8)"), Product.class);
        assertTrue(iter.hasNext());
        Page<Product> page = iter.next();
        assertEquals(8, page.getData().size());
        assertTrue(page.getAfter().isEmpty());
        assertFalse(iter.hasNext());
        assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    public void query_all_with_manual_pagination() {
        // Demonstrate how a user could paginate without PageIterator.
        PageOf<Product> pageOf = new PageOf<>(Product.class);
        QuerySuccess<Page<Product>> first = client.query(fql("Product.all()"), pageOf);
        Page<Product> latest = first.getData();
        List<List<Product>> pages = new ArrayList<>();

        pages.add(latest.getData());
        while (latest != null) {
            latest = latest.getAfter().map(after -> {
                Page<Product> page = client.queryPage(after, Product.class, null).getData();
                pages.add(page.getData());
                return page;
            }).orElse(null);
        }
        assertEquals(4, pages.size());
        assertEquals(2, pages.get(3).size());
    }

    @Test
    public void query_all_with_pagination() {
        PageIterator<Product> iter = client.paginate(fql("Product.all()"), Product.class);
        List<Page<Product>> pages = new ArrayList<>();
        iter.forEachRemaining(pages::add);
        assertEquals(4, pages.size());
        List<Product> products = pages.stream().flatMap(p -> p.getData().stream()).collect(Collectors.toList());
        assertEquals(50, products.size());
    }

    @Test
    public void query_all_flattened() {
        PageIterator<Product> iter = client.paginate(fql("Product.all()"), Product.class);
        Iterator<Product> productIter = iter.flatten();
        List<Product> products = new ArrayList<>();
        // Java iterators not being iterable (or useable in a for-each loop) is annoying.
        for (Product p : (Iterable<Product>) () -> productIter) {
            products.add(p);
        }
        assertEquals(50, products.size());
    }

    @Test
    public void query_statsAreTrackedForExplicitPagination() {
        var cfg = FaunaConfig.builder()
                .secret("secret")
                .endpoint("http://localhost:8443")
                .defaultStatsCollector()
                .build();
        var client = Fauna.client(cfg);
        PageIterator<Product> iter = client.paginate(fql("Product.all()"), Product.class);
        iter.forEachRemaining(page -> {});

        var stats = client.getStatsCollector().get().read();
        assertEquals(82, stats.getReadOps());
        assertEquals(4, stats.getComputeOps());
    }

    @Test
    public void query_statsAreTrackedForFlattenedPagination() {
        var cfg = FaunaConfig.builder()
                .secret("secret")
                .endpoint("http://localhost:8443")
                .defaultStatsCollector()
                .build();
        var client = Fauna.client(cfg);
        PageIterator<Product> iter = client.paginate(fql("Product.all()"), Product.class);
        Iterator<Product> productIter = iter.flatten();
        for (Product p : (Iterable<Product>) () -> productIter) {
        }

        var stats = client.getStatsCollector().get().read();
        assertEquals(82, stats.getReadOps());
        assertEquals(4, stats.getComputeOps());
    }
}
