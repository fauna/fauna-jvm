package com.fauna.exception;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.ConstraintFailure;
import com.fauna.response.ConstraintFailure.PathElement;
import com.fauna.response.QueryStats;
import com.fauna.response.wire.QueryResponseWire;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.fauna.exception.ErrorHandler.handleErrorResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private String getQueryResponseWire(List<List<Object>> paths) {
        ObjectNode body = mapper.createObjectNode();
        QueryStats stats = new QueryStats(0, 0, 0, 0, 0, 0, 0, 0, List.of());
        // body.put("stats", mapper.writeValueAsString(stats));
        body.putPOJO("stats", stats);
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
    public void TestConstraintFailureFromBodyUsingParser() throws IOException {
        String failureWire = constraintFailure(List.of(List.of("pathElement"))).toString();
        ConstraintFailure failure = ConstraintFailure.parse(JSON_FACTORY.createParser(failureWire));
        ConstraintFailure.PathElement[][] actualFailures = failure.getPaths().get();
        assertEquals(new PathElement[]{new PathElement("hello")}, actualFailures);

    }

    @Test
    public void TestConstraintFailureFromBodyWithPath() throws JsonProcessingException {
        List<List<Object>> expected = List.of(List.of("name"));

        String body = getQueryResponseWire(expected);
        QueryResponseWire res = mapper.readValue(body, QueryResponseWire.class);
        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class, () -> handleErrorResponse(400, res, ""));
        assertEquals(expected, exc.getConstraintFailures().get(0).getPaths());
    }

    @Test
    public void TestConstraintFailureFromBodyWithIntegerInPath() throws JsonProcessingException {
        List<List<Object>> expected = List.of(List.of("name"), List.of("name2", 1, 2, "name3"));
        String body = getQueryResponseWire(expected);
        QueryResponseWire res = mapper.readValue(body, QueryResponseWire.class);
        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class, () -> handleErrorResponse(400, res, ""));
        assertEquals(expected, exc.getConstraintFailures().get(0).getPaths());
    }
}
