package com.fauna.e2e;

import com.fauna.client.FaunaClient;
import static com.fauna.query.builder.Query.fql;

public class Fixtures {

    public static void ProductCollection(FaunaClient client) {
    try {
        client.query(fql("Collection.byName('Product')?.delete()"));
    } catch (Exception e) {}
    client.query(fql("Collection.create({name: 'Product', fields: {'name': {signature: 'String'},'quantity': {signature: 'Int', default: '0'}}, constraints: [{unique: ['name']},{check:{name: 'posQuantity', body: '(doc) => doc.quantity >= 0' }}]})"));
    }

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



}
