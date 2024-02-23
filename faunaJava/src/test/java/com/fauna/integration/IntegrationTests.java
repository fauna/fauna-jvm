package com.fauna.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fauna.beans.Person;
import com.fauna.client.FaunaClient;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class IntegrationTests {

    private static FaunaClient client;

    @BeforeAll
    public static void setUp() {
        client = FaunaClient.builder()
            .withSecret("your_fauna_secret_here")
            .build();
    }

    @Test
    public void userDefinedObjectTest() throws ExecutionException, InterruptedException {
        Person expected = new Person("Georgia", "O'Keeffe", 136);
        Expr query = Language.query(Language.toString(expected));
        Value result = client.query(query).get();
        Person actual = result.at("data").to(Person.class).get();

        assertNotEquals(expected, actual);
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getAge(), actual.getAge());
    }

    @Test
    public void paginateSinglePageWithSmallCollection()
        throws ExecutionException, InterruptedException {
        Expr query = Language.query("[1,2,3,4,5,6,7,8,9,10]");
        Value paginatedResult = client.query(query).get();

        assertEquals(1, paginatedResult.at("data").size());
        assertEquals(10, paginatedResult.at("data", 0).to(List.class).get().size());
    }

    @Test
    public void paginateMultiplePagesWithCollection()
        throws ExecutionException, InterruptedException {
        Expr query = Language.query("[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20]");
        Value paginatedResult = client.query(query).get();

        assertEquals(2, paginatedResult.at("data").size());
        assertEquals(16, paginatedResult.at("data", 0).to(List.class).get().size());
        assertEquals(4, paginatedResult.at("data", 1).to(List.class).get().size());
    }

    @Test
    public void paginateMultiplePagesWithPocoCollection()
        throws ExecutionException, InterruptedException {
        // Assuming Person class is defined with appropriate constructor and getters
        List<Person> items = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            items.add(new Person("FirstName" + i, "LastName" + i, i));
        }
        Expr query = Language.query(Language.toString(items));
        Value paginatedResult = client.query(query).get();

        assertEquals(5, paginatedResult.at("data").size());
        assertEquals(20, paginatedResult.at("data", 0).to(List.class).get().size());
        assertEquals(20, paginatedResult.at("data", 1).to(List.class).get().size());
        assertEquals(20, paginatedResult.at("data", 2).to(List.class).get().size());
        assertEquals(20, paginatedResult.at("data", 3).to(List.class).get().size());
        assertEquals(20, paginatedResult.at("data", 4).to(List.class).get().size());
    }

    @Test
    public void paginateIteratorCanBeFlattened() throws ExecutionException, InterruptedException {
        // Assuming Person class is defined with appropriate constructor and getters
        List<Person> items = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            items.add(new Person("FirstName" + i, "LastName" + i, i));
        }
        Expr query = Language.query(Language.toString(items));
        Value paginatedResult = client.query(query).get();

        assertEquals(100, paginatedResult.at("data").to(List.class).get().size());
    }
}
