package com.fauna.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.QueryStats;
import com.fauna.response.wire.QueryResponseWire;
import org.junit.Test;

import java.util.List;

import static com.fauna.exception.ErrorHandler.handleErrorResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConstraintFailureTest {
    ObjectMapper mapper = new ObjectMapper();

    private QueryResponseWire getQueryResponseWire(List<List<Object>> paths) throws JsonProcessingException {
        ObjectNode body = mapper.createObjectNode();
        QueryStats stats = new QueryStats(0, 0, 0, 0, 0, 0, 0, List.of());
        // body.put("stats", mapper.writeValueAsString(stats));
        body.putPOJO("stats", stats);
        body.put("summary", "error: failed to...");
        body.put("txn_ts", 1723490275035000L);
        body.put("schema_version", 1723490246890000L);
        ObjectNode error = body.putObject("error");
        error.put("code", "constraint_failure");
        error.put("message", "Failed to create... ");

        ArrayNode failures = error.putArray("constraint_failures");
        ObjectNode failure = failures.addObject();
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
        return mapper.readValue(body.toString(), QueryResponseWire.class);
    }

    @Test
    public void TestConstraintFailureFromBodyWithPath() throws JsonProcessingException {
        List<List<Object>> expected = List.of(List.of("name"));
        var res = getQueryResponseWire(expected);
        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class, () -> handleErrorResponse(400, res, ""));
        assertEquals(expected, exc.getConstraintFailures().get(0).getPaths());
    }

    @Test
    public void TestConstraintFailureFromBodyWithIntegerInPath() throws JsonProcessingException {
        List<List<Object>> expected = List.of(List.of("name"), List.of("name2", 1, 2, "name3"));
        var res = getQueryResponseWire(expected);
        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class, () -> handleErrorResponse(400, res, ""));
        assertEquals(expected, exc.getConstraintFailures().get(0).getPaths());
    }
}
