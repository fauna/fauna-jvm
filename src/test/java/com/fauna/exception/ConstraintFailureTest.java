package com.fauna.exception;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.ConstraintFailure;
import com.fauna.response.QueryResponse;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConstraintFailureTest {
    ObjectMapper mapper = new ObjectMapper();
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private ObjectNode constraintFailure(List<List<Object>> paths) {
        ObjectNode failure = mapper.createObjectNode();
        ArrayNode pathArray = failure.putArray("paths");
        for (List<Object> path : paths) {
            ArrayNode pathNode = mapper.createArrayNode();
            for (Object pathElement : path) {
                if (pathElement instanceof String) {
                    pathNode.add((String) pathElement);
                } else if (pathElement instanceof Integer) {
                    pathNode.add((Integer) pathElement);
                } else {
                    throw new RuntimeException();
                }
            }
            pathArray.add(pathNode);
        }
        failure.put("message", "Document failed check constraint");
        return failure;
    }

    private String getConstraintFailureBody(List<List<Object>> paths) throws JsonProcessingException {
        ObjectNode body = mapper.createObjectNode();
        ObjectNode stats = body.putObject("stats");
        stats.put("compute_ops", 100);
        // body.putPOJO("stats", stats);
        body.put("summary", "error: failed to...");
        body.put("txn_ts", 1723490275035000L);
        body.put("schema_version", 1723490246890000L);
        ObjectNode error = body.putObject("error");
        error.put("code", "constraint_failure");
        error.put("message", "Failed to create... ");
        ArrayNode failures = error.putArray("constraint_failures");
        failures.add(constraintFailure(paths));
        return body.toString();
    }

    @Test
    public void testPathElementEquality() {
        ConstraintFailure.PathElement one = new ConstraintFailure.PathElement("1");
        ConstraintFailure.PathElement two = new ConstraintFailure.PathElement("1");
        ConstraintFailure.PathElement three = new ConstraintFailure.PathElement("3");
        assertEquals(one, two);
        assertNotEquals(one, three);
    }

    @Test
    public void testConstraintFailureEquality() {
        ConstraintFailure one = ConstraintFailure.builder().message("hell").path(ConstraintFailure.createPath("one", 2)).build();
        ConstraintFailure two = ConstraintFailure.builder().message("hell").path(ConstraintFailure.createPath("one", 2)).build();
        assertEquals(one.getMessage(), two.getMessage());
        assertEquals(one.getName(), two.getName());
        assertArrayEquals(one.getPaths().orElseThrow(), two.getPaths().orElseThrow());
        assertEquals(one, two);
    }

    @Test
    public void TestConstraintFailureFromBodyUsingParser() throws IOException {
        String failureWire = constraintFailure(List.of(List.of("pathElement"))).toString();
        ConstraintFailure failure = ConstraintFailure.parse(JSON_FACTORY.createParser(failureWire));
        assertEquals(Optional.of(List.of("pathElement")), failure.getPathStrings());
    }

    @Test
    public void TestConstraintFailureFromBodyWithPath() throws JsonProcessingException {
        List<List<Object>> expected = List.of(List.of("name"));

        String body = getConstraintFailureBody(expected);
        HttpResponse<InputStream> resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn(new ByteArrayInputStream(body.getBytes()));
        when(resp.statusCode()).thenReturn(400);
        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class,() -> QueryResponse.parseResponse(resp, null));
        assertEquals(Optional.of(List.of("name")), exc.getConstraintFailures()[0].getPathStrings());
    }

    @Test
    public void TestConstraintFailureFromBodyWithIntegerInPath() throws JsonProcessingException {
        List<List<Object>> expected = List.of(List.of("name"), List.of("name2", 1, 2, "name3"));

        String body = getConstraintFailureBody(expected);
        HttpResponse<InputStream> resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn(new ByteArrayInputStream(body.getBytes()));
        when(resp.statusCode()).thenReturn(400);
        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class,() -> QueryResponse.parseResponse(resp, null));
        assertEquals(Optional.of(List.of("name", "name2.1.2.name3")), exc.getConstraintFailures()[0].getPathStrings());
    }

}
