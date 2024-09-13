package com.fauna.exception;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.fauna.response.ConstraintFailure;
import com.fauna.response.ErrorInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorInfoTest {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final ObjectMapper MAPPER = new ObjectMapper(JSON_FACTORY);

    public ArrayNode addPaths(ObjectNode failure, List<List<Object>> elements) {
        ArrayNode paths = failure.putArray("paths");
        for (List<Object> pathElements : elements) {
            ArrayNode path = paths.addArray();
            pathElements.forEach(e -> path.add(e.toString()));
        }
        return paths;
    }

    public ObjectNode buildConstraintFailureError() {
        ObjectNode infoJson = MAPPER.createObjectNode();
        infoJson.put("code", "constraint_failure");
        infoJson.put("message", "Constraint Failure.");
        return infoJson;
    }

    @Test
    public void testParseWithMinimalConstraintFailure() throws IOException {

        ObjectNode infoJson = buildConstraintFailureError();
        ArrayNode failures = infoJson.putArray("constraint_failures");
        ObjectNode failure1 = failures.addObject();
        failure1.put("message", "msg1");

        ErrorInfo info = ErrorInfo.parse(JSON_FACTORY.createParser(infoJson.toString()));
        assertEquals("Constraint Failure.", info.getMessage());
        assertEquals("constraint_failure", info.getCode());
        assertTrue(info.getConstraintFailures().isPresent());
        assertEquals(1, info.getConstraintFailures().orElseThrow().length);
        ConstraintFailure constraintFailure = info.getConstraintFailures().get()[0];
        assertEquals("msg1", constraintFailure.getMessage());
        assertTrue(constraintFailure.getName().isEmpty());
        assertTrue(constraintFailure.getPaths().isEmpty());
    }

    @Test
    public void testParseWithMultipleConstraintFailures() throws IOException {

        ObjectNode infoJson = buildConstraintFailureError();
        ArrayNode failures = infoJson.putArray("constraint_failures");
        ObjectNode obj1 = failures.addObject();
        obj1.put("message", "msg1");
        obj1.put("name", "name1");
        ObjectNode obj2 = failures.addObject();
        obj2.put("message", "msg2");

        ErrorInfo info = ErrorInfo.parse(JSON_FACTORY.createParser(infoJson.toString()));
        assertEquals("Constraint Failure.", info.getMessage());
        assertEquals("constraint_failure", info.getCode());
        assertTrue(info.getConstraintFailures().isPresent());
        assertEquals(2, info.getConstraintFailures().orElseThrow().length);
        ConstraintFailure constraintFailure = info.getConstraintFailures().get()[0];
        assertEquals("msg1", constraintFailure.getMessage());
        assertEquals("name1", constraintFailure.getName().orElseThrow());
        assertTrue(constraintFailure.getPaths().isEmpty());

        ConstraintFailure failure2 = info.getConstraintFailures().get()[1];
        assertEquals("msg2", failure2.getMessage());
        assertTrue(failure2.getName().isEmpty());
        assertTrue(failure2.getPaths().isEmpty());
    }

    @Test
    public void testParseWithConstraintFailuresWithPaths() throws IOException {

        ObjectNode infoJson = buildConstraintFailureError();
        ArrayNode failures = infoJson.putArray("constraint_failures");
        ObjectNode obj1 = failures.addObject();
        obj1.put("message", "msg1");
        addPaths(obj1, List.of(List.of(1, "a"), List.of("1b")));

        ObjectNode obj2 = failures.addObject();
        obj2.put("message", "msg2");
        addPaths(obj2, List.of(List.of(2, "a")));

        ErrorInfo info = ErrorInfo.parse(JSON_FACTORY.createParser(infoJson.toString()));
        assertEquals("Constraint Failure.", info.getMessage());
        assertEquals("constraint_failure", info.getCode());
        assertTrue(info.getConstraintFailures().isPresent());
        assertEquals(2, info.getConstraintFailures().orElseThrow().length);
        ConstraintFailure failure1 = info.getConstraintFailures().get()[0];
        assertTrue(failure1.getPaths().isPresent());
        assertEquals("msg1", failure1.getMessage());
        assertTrue(failure1.getName().isEmpty());
        assertEquals(List.of("1.a", "1b"), failure1.getPathStrings().orElseThrow());

        ConstraintFailure failure2 = info.getConstraintFailures().get()[1];
        assertEquals("msg2", failure2.getMessage());
        assertTrue(failure2.getName().isEmpty());
        assertEquals(List.of("2.a"), failure2.getPathStrings().orElseThrow());
    }

}
