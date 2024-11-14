package com.fauna.exception;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.codec.Codec;
import com.fauna.codec.DefaultCodecProvider;
import com.fauna.codec.UTF8FaunaParser;
import com.fauna.response.ConstraintFailure;
import com.fauna.response.ErrorInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
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

    public ObjectNode buildError(String code, String message) {
        ObjectNode infoJson = MAPPER.createObjectNode();
        infoJson.put("code", code);
        infoJson.put("message", message);
        return infoJson;
    }

    @Test
    public void testParseSimpleError() throws IOException {
        ObjectNode errorJson =
                buildError("invalid_request", "Invalid Request!");
        ErrorInfo info = ErrorInfo.parse(
                JSON_FACTORY.createParser(errorJson.toString()));
        assertEquals("invalid_request", info.getCode());
        assertEquals("Invalid Request!", info.getMessage());
        assertTrue(info.getAbort(null).isEmpty());
        assertTrue(info.getAbortJson().isEmpty());
        assertTrue(info.getConstraintFailures().isEmpty());
    }

    @Test
    public void testParseWithAbortData() throws IOException {
        ObjectNode infoJson = buildError("abort", "aborted!");
        ObjectNode abortData = infoJson.putObject("abort");
        abortData.put("@time", "2023-04-06T03:33:32.226Z");
        ErrorInfo info =
                ErrorInfo.parse(JSON_FACTORY.createParser(infoJson.toString()));
        assertEquals("abort", info.getCode());
        TreeNode tree = info.getAbortJson().get();
        UTF8FaunaParser parser = new UTF8FaunaParser(tree.traverse());
        Codec<Instant> instantCodec =
                DefaultCodecProvider.SINGLETON.get(Instant.class);
        parser.read();
        Instant abortTime = instantCodec.decode(parser);
        assertEquals(1680752012226L, abortTime.toEpochMilli());
    }

    @Test
    public void testParseAndGetAbortData() throws IOException {
        ObjectNode infoJson = buildError("abort", "aborted!");
        ObjectNode abortData = infoJson.putObject("abort");
        abortData.put("@time", "2023-04-06T03:33:32.226Z");
        ErrorInfo info =
                ErrorInfo.parse(JSON_FACTORY.createParser(infoJson.toString()));
        assertEquals("abort", info.getCode());
        Instant abortTime = info.getAbort(Instant.class).get();
        assertEquals(1680752012226L, abortTime.toEpochMilli());
    }

    @Test
    public void testParseWithMinimalConstraintFailure() throws IOException {
        ObjectNode infoJson =
                buildError("constraint_failure", "Constraint failed!");
        ArrayNode failures = infoJson.putArray("constraint_failures");
        ObjectNode failure1 = failures.addObject();
        failure1.put("message", "msg1");

        ErrorInfo info =
                ErrorInfo.parse(JSON_FACTORY.createParser(infoJson.toString()));
        assertTrue(info.getAbortJson().isEmpty());
        assertTrue(info.getAbort(String.class).isEmpty());
        assertEquals("Constraint failed!", info.getMessage());
        assertEquals("constraint_failure", info.getCode());
        assertTrue(info.getConstraintFailures().isPresent());
        assertEquals(1, info.getConstraintFailures().orElseThrow().length);
        ConstraintFailure constraintFailure =
                info.getConstraintFailures().get()[0];
        assertEquals("msg1", constraintFailure.getMessage());
        assertTrue(constraintFailure.getName().isEmpty());
        assertTrue(constraintFailure.getPaths().isEmpty());
    }

    @Test
    public void testParseWithMultipleConstraintFailures() throws IOException {
        ObjectNode infoJson =
                buildError("constraint_failure", "Constraint failed!");
        ArrayNode failures = infoJson.putArray("constraint_failures");
        ObjectNode obj1 = failures.addObject();
        obj1.put("message", "msg1");
        obj1.put("name", "name1");
        ObjectNode obj2 = failures.addObject();
        obj2.put("message", "msg2");

        ErrorInfo info =
                ErrorInfo.parse(JSON_FACTORY.createParser(infoJson.toString()));
        assertEquals("Constraint failed!", info.getMessage());
        assertEquals("constraint_failure", info.getCode());
        assertTrue(info.getConstraintFailures().isPresent());
        assertEquals(2, info.getConstraintFailures().orElseThrow().length);
        ConstraintFailure constraintFailure =
                info.getConstraintFailures().get()[0];
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
        ObjectNode infoJson =
                buildError("constraint_failure", "Constraint failed!");
        ArrayNode failures = infoJson.putArray("constraint_failures");
        ObjectNode obj1 = failures.addObject();
        obj1.put("message", "msg1");
        addPaths(obj1, List.of(List.of(1, "a"), List.of("1b")));

        ObjectNode obj2 = failures.addObject();
        obj2.put("message", "msg2");
        addPaths(obj2, List.of(List.of(2, "a")));

        ErrorInfo info =
                ErrorInfo.parse(JSON_FACTORY.createParser(infoJson.toString()));
        assertEquals("Constraint failed!", info.getMessage());
        assertEquals("constraint_failure", info.getCode());
        assertTrue(info.getConstraintFailures().isPresent());
        assertEquals(2, info.getConstraintFailures().orElseThrow().length);
        ConstraintFailure failure1 = info.getConstraintFailures().get()[0];
        assertTrue(failure1.getPaths().isPresent());
        assertEquals("msg1", failure1.getMessage());
        assertTrue(failure1.getName().isEmpty());
        assertEquals(List.of("1.a", "1b"),
                failure1.getPathStrings().orElseThrow());

        ConstraintFailure failure2 = info.getConstraintFailures().get()[1];
        assertEquals("msg2", failure2.getMessage());
        assertTrue(failure2.getName().isEmpty());
        assertEquals(List.of("2.a"), failure2.getPathStrings().orElseThrow());
    }

}
