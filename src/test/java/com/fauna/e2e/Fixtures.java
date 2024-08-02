package com.fauna.e2e;

import com.fauna.client.FaunaClient;

import java.util.Map;
import java.util.stream.IntStream;

import static com.fauna.query.builder.Query.fql;

public class Fixtures {

    public static void PeopleDatabase(FaunaClient client) {
        client.query(fql("Database.byName('People')?.delete()"));
        client.query(fql("Database.create({name: 'People'})"));
    }

    public static void PersonCollection(FaunaClient client) {
        client.query(fql("Collection.byName('Author')?.delete()"));
        client.query(fql("Collection.create({name: 'Author'})"));
        client.query(fql("Author.create({'firstName': 'Alice', 'lastName': 'Wonderland', 'middleInitial': 'N', 'age': 65})"));
        client.query(fql("Author.create({'firstName': 'Mad', 'lastName': 'Atter', 'middleInitial': 'H', 'age': 90})"));
    }

    public static void ProductCollection(FaunaClient client) {
        client.query(fql("Collection.byName('Product')?.delete()"));
        client.query(fql("Collection.create({name: 'Product'})"));
        IntStream.range(0, 50).forEach(i -> client.query(
                fql("Product.create({'name': ${name}, 'quantity': ${quantity}})",
                        Map.of("name", "product-" + i, "quantity", i))));
    }



}
