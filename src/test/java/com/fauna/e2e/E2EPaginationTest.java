package com.fauna.e2e;

import com.fauna.client.Fauna;
import com.fauna.client.FaunaClient;
import com.fauna.client.PageIterator;
import com.fauna.e2e.beans.Product;
import com.fauna.response.QuerySuccess;
import com.fauna.serialization.generic.PageOf;
import com.fauna.types.Page;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.fauna.query.builder.Query.fql;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class E2EPaginationTest {

    public static final FaunaClient client = Fauna.local();

    @BeforeAll
    public static void setup() {
        Fixtures.ProductCollection(client);
    }

    @Test
    public void query_single_item() {
        QuerySuccess<Product> typed = client.query(fql("Product.firstWhere(.name == 'product-1')"), Product.class);
        Product product = typed.getData();
        assertEquals("product-1", product.getName());
        assertEquals(1, product.getQuantity());
    }

    @Test
    public void query_all_with_manual_pagination() {
        Class cls = Product.class;

        PageOf<Product> pageOf = new PageOf<Product>(cls);
        QuerySuccess<Page<Product>> first = client.query(fql("Product.all()"), pageOf);
        Page<Product> latest = first.getData();
        List<List<Product>> pages = new ArrayList<>();

        pages.add(latest.data());
        while (latest.after() != null) {
            QuerySuccess<Page<Product>> paged = client.query(fql("Set.paginate(${after})", Map.of("after", latest.after())), pageOf);
            latest = paged.getData();
            pages.add(latest.data());
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
        List<Product> products = pages.stream().flatMap(p -> p.data().stream()).collect(Collectors.toList());
        assertEquals(50, products.size());
    }

    @Test
    public void query_all_flattened() {
        PageIterator<Product> iter = client.paginate(fql("Product.all()"), Product.class);
        Iterator<Product> productIter = iter.flatten();
        List<Product> products = StreamSupport.stream((
                (Iterable<Product>)() -> productIter).spliterator(), false).collect(Collectors.toList());
        assertEquals(50, products.size());
    }
}
