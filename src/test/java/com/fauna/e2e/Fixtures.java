package com.fauna.e2e;

import com.fauna.client.FaunaClient;
import static com.fauna.query.builder.Query.fql;

public class Fixtures {

    public static void PersonCollection(FaunaClient client) {
        client.query(fql("Collection.byName('Author')?.delete()"));
        client.query(fql("Collection.create({name: 'Author'})"));
        client.query(fql("Author.create({'firstName': 'Alice', 'lastName': 'Wonderland', 'middleInitial': 'N', 'age': 65})"));
        client.query(fql("Author.create({'firstName': 'Mad', 'lastName': 'Atter', 'middleInitial': 'H', 'age': 90})"));
    }



}
