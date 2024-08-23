package com.fauna.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.QueryStats;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.fauna.exception.ErrorHandler.handleErrorResponse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConstraintFailureTest {
    ObjectMapper mapper = new ObjectMapper();

    private String constraintFailureBody(Object[][] paths) {
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
        for (Object[] path : paths) {
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
        return body.toString();
    }


//    @Test
//    public void TestConstraintFailureFromBody() throws JsonProcessingException {
//        String body = constraintFailureBody(new Object[][]{});
//        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class, () -> handleErrorResponse(400, body, mapper));
//    }
//
//    @Test
//    public void TestConstraintFailureFromBodyWithPath() throws IOException {
//        Object[][] pathArray = new Object[][]{{"name"}};
//        String body = constraintFailureBody(pathArray);
//        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class, () -> handleErrorResponse(400, body, mapper));
//        assertEquals(pathArray, exc.getConstraintFailures()[0].getPaths().get());
//    }
//
//    @Test
//    public void TestConstraintFailureFromBodyWithIntegerInPath() {
//        Object[][] pathArray = new Object[][]{{"name"}, {"name2", 1, 2, "name3"}};
//        String body = constraintFailureBody(pathArray);
//        ConstraintFailureException exc = assertThrows(ConstraintFailureException.class, () -> handleErrorResponse(400, body, mapper));
//        assertArrayEquals(pathArray, exc.getConstraintFailures()[0].getPaths().get());
//    }
}
