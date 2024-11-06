package com.fauna.e2e;

import com.fauna.client.FaunaClient;

import java.util.Map;
import java.util.stream.IntStream;

import static com.fauna.query.builder.Query.fql;

public class Fixtures {

    public static void PeopleDatabase(FaunaClient client) {
        client.asyncQuery(fql("Database.byName('People')?.delete()"))
                .exceptionally(t -> null).join();
        client.query(fql("Database.create({name: 'People'})"));
    }

    public static void PersonCollection(FaunaClient client) {
        client.asyncQuery(fql("Collection.byName('Author')?.delete()"))
                .exceptionally(t -> null).join();
        client.query(fql("Collection.create({name: 'Author'})"));
        client.query(
                fql("Author.create({'firstName': 'Alice', 'lastName': 'Wonderland', 'middleInitial': 'N', 'age': 65})"));
        client.query(
                fql("Author.create({'firstName': 'Mad', 'lastName': 'Atter', 'middleInitial': 'H', 'age': 90})"));
    }

    public static long ProductCollection(FaunaClient client) {
        client.asyncQuery(fql("Collection.byName('Product')?.delete()"))
                .exceptionally(t -> null).join();
        client.query(
                fql("Collection.create({name: 'Product', fields: {'name': {signature: 'String'},'quantity': {signature: 'Int', default: '0'}}, constraints: [{unique: ['name']},{check:{name: 'posQuantity', body: '(doc) => doc.quantity >= 0' }}]})"));
        // For testing the event feed API, we need to know a timestamp that's after the collection was created, but
        // before any items are added to it.
        long collectionTs = client.getLastTransactionTs().orElseThrow();
        IntStream.range(0, 50).forEach(i -> client.query(
                fql("Product.create({'name': ${name}, 'quantity': ${quantity}})",
                        Map.of("name", "product-" + i, "quantity", i))));
        return collectionTs;
    }
}
