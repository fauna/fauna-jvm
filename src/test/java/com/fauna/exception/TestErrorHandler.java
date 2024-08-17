package com.fauna.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.response.QueryStats;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestErrorHandler {

    ObjectMapper mapper = new ObjectMapper();
    QueryStats stats = new QueryStats(0, 0, 0, 0,
            0, 0, 0, List.of());

    public static class TestArgs {
        public int httpStatus;
        public String code;
        public Class exception;
        public TestArgs(int httpStatus, String code, Class exception) {
            this.httpStatus = httpStatus;
            this.code = code;
            this.exception = exception;
        }
    }

    public static Stream<TestArgs> testArgStream() {
        return Stream.of(
                new TestArgs(400, "unbound_variable", QueryRuntimeException.class),
                new TestArgs(400, "invalid_query", QueryCheckException.class),
                new TestArgs(400, "limit_exceeded", ThrottlingException.class),
                new TestArgs(400, "invalid_request", InvalidRequestException.class),
                new TestArgs(400, "abort", AbortException.class),
                new TestArgs(400, "constraint_failure", ConstraintFailureException.class),
                new TestArgs(401, "unauthorized", AuthenticationException.class),
                new TestArgs(403, "forbidden", AuthorizationException.class),
                new TestArgs(409, "contended_transaction", ContendedTransactionException.class),
                new TestArgs(440, "time_out", QueryTimeoutException.class),
                new TestArgs(500, "internal_error", ServiceInternalException.class),
                new TestArgs(503, "time_out", QueryTimeoutException.class),
                // Unknown error code results in ProtocolException, except in case of 400.
                new TestArgs(400, "unknown_code", QueryRuntimeException.class),
                new TestArgs(401, "unknown_code", ProtocolException.class),
                new TestArgs(500, "unknown_code", ProtocolException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("testArgStream")
    public void testHandleBadRequest(TestArgs args) throws JsonProcessingException {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        ObjectNode stats = root.putObject("stats");
        error.put("code", args.code);
        String body = mapper.writeValueAsString(root);
        assertThrows(args.exception, () -> ErrorHandler.handleErrorResponse(args.httpStatus, body, mapper));
    }

    public void testHandleInvalidJson() {
        assertThrows(ProtocolException.class,
                () -> ErrorHandler.handleErrorResponse(400, "{not json", mapper));
    }

    public void testMissingStats() throws JsonProcessingException {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode error = root.putObject("error");
        error.put("code", "invalid_query");
        String body = mapper.writeValueAsString(root);
        assertThrows(ProtocolException.class,
                () -> ErrorHandler.handleErrorResponse(400, body, mapper));
    }
}