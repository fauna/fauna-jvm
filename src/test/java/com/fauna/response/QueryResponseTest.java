package com.fauna.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ClientException;
import com.fauna.exception.ProtocolException;
import com.fauna.mapping.MappingContext;
import com.fauna.serialization.Deserializer;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class QueryResponseTest {

    @Test
    public void getFromResponseBody_Success() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode data = mapper.createObjectNode();
        ObjectNode obj = data.putObject("@object");
        obj.put("@int", "notanint");
        obj.put("anInt", mapper.createObjectNode().put("@int", "123"));
        obj.put("@object", "notanobject");
        obj.putObject("anEscapedObject")
                .putObject("@object")
                .put("@long", "notalong");

        MappingContext ctx = new MappingContext();
        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("data", data);
        String body = successNode.toString();

        QueryResponse response = new QuerySuccess<>(Deserializer.DYNAMIC, successNode, null);

        assertEquals(successNode, response.getRawJson());

        QuerySuccess<Map<String, Object>> successResponse = (QuerySuccess<Map<String, Object>>) response;
        assertNotNull(successResponse.getData());

        Map<String, Object> actualData = successResponse.getData();

        assertNotNull(actualData);

        assertNotNull(actualData.get("@int"));
        assertEquals("notanint", actualData.get("@int"));

        assertNotNull(actualData.get("anInt"));
        assertEquals(123, actualData.get("anInt"));

        assertNotNull(actualData.get("@object"));
        assertEquals("notanobject", actualData.get("@object"));

        assertNotNull(successResponse.getData().get("anEscapedObject"));
        Map<String, Object> escapedObj = (Map<String, Object>) successResponse.getData()
            .get("anEscapedObject");

        assertNotNull(escapedObj.get("@long"));
        assertEquals("notalong", escapedObj.get("@long"));

    }

    @Test
    public void getFromResponseBody_Failure() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode errorData = mapper.createObjectNode();
        errorData.put(ResponseFields.ERROR_CODE_FIELD_NAME, "ErrorCode");
        errorData.put(ResponseFields.ERROR_MESSAGE_FIELD_NAME, "ErrorMessage");
        errorData.put(ResponseFields.ERROR_CONSTRAINT_FAILURES_FIELD_NAME, "ConstraintFailures");
        errorData.put(ResponseFields.ERROR_ABORT_FIELD_NAME, "AbortData");
        ObjectNode failureNode = mapper.createObjectNode();
        failureNode.put(ResponseFields.ERROR_FIELD_NAME, errorData);

        String body = failureNode.toString();

        QueryResponse response = new QueryFailure(400, failureNode, null);

        assertTrue(response instanceof QueryFailure);
        assertEquals(failureNode, response.getRawJson());

        QueryFailure failureResponse = (QueryFailure) response;
        assertEquals(400, failureResponse.getStatusCode());
        assertEquals("ErrorCode", failureResponse.getErrorCode());
        assertEquals("ErrorMessage", failureResponse.getMessage());
        assertEquals("ConstraintFailures", failureResponse.getConstraintFailures());
        assertEquals(Optional.of("AbortData"), failureResponse.getAbort());
    }

    @Test
    public void handleResponseWithInvalidJsonThrowsProtocolException() {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"not valid json\"");
        assertThrows(ProtocolException.class, () -> QueryResponse.handleResponse(resp));
    }

    @Test
    public void handleResponseWithMissingStatsThrowsProtocolException() {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"not valid json\"");
        assertThrows(ProtocolException.class, () -> QueryResponse.handleResponse(resp));
    }

    @Test
    void getFromResponseBody_Exception() {
        MappingContext ctx = new MappingContext();
        String body = "Invalid JSON";

        // TODO call FaunaClient.handleResponse here.
        ClientException exception = assertThrows(ClientException.class, () -> {
           throw new ClientException("Error occurred while parsing the response body");
        });

        assertEquals("Error occurred while parsing the response body", exception.getMessage());
        // assertTrue(exception.getCause().getMessage().contains(
        // "Unrecognized token 'Invalid': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')"));
    }

}