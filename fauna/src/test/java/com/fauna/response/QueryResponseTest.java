package com.fauna.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fauna.common.constants.ResponseFields;
import com.fauna.exception.SerializationException;
import com.fauna.mapping.MappingContext;
import com.fauna.serialization.Deserializer;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QueryResponseTest {

    @Test
    void getFromResponseBody_Success() {
        String data = "{\n" +
            "    \"@object\": {\n" +
            "        \"@int\": \"notanint\",\n" +
            "        \"anInt\": { \"@int\": \"123\" },\n" +
            "        \"@object\": \"notanobject\",\n" +
            "        \"anEscapedObject\": { \"@object\": { \"@long\": \"notalong\" } }\n" +
            "    }\n" +
            "}";

        MappingContext ctx = new MappingContext();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode successNode = mapper.createObjectNode();
        successNode.put("data", data);
        String body = successNode.toString();

        QueryResponse response = QueryResponse.getFromResponseBody(ctx, Deserializer.DYNAMIC, 200,
            body);

        assertTrue(response instanceof QuerySuccess);
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
    void getFromResponseBody_Failure() {
        MappingContext ctx = new MappingContext();
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode errorData = mapper.createObjectNode();
        errorData.put(ResponseFields.ERROR_CODE_FIELD_NAME, "ErrorCode");
        errorData.put(ResponseFields.ERROR_MESSAGE_FIELD_NAME, "ErrorMessage");
        errorData.put(ResponseFields.ERROR_CONSTRAINT_FAILURES_FIELD_NAME, "ConstraintFailures");
        errorData.put(ResponseFields.ERROR_ABORT_FIELD_NAME, "AbortData");
        ObjectNode failureNode = mapper.createObjectNode();
        failureNode.put(ResponseFields.ERROR_FIELD_NAME, errorData);

        String body = failureNode.toString();

        QueryResponse response = QueryResponse.getFromResponseBody(ctx, Deserializer.DYNAMIC, 400,
            body);

        assertTrue(response instanceof QueryFailure);
        assertEquals(failureNode, response.getRawJson());

        QueryFailure failureResponse = (QueryFailure) response;
        assertEquals(400, failureResponse.getStatusCode());
        assertEquals("ErrorCode", failureResponse.getErrorCode());
        assertEquals("ErrorMessage", failureResponse.getMessage());
        assertEquals("ConstraintFailures", failureResponse.getConstraintFailures());
        assertEquals("AbortData", failureResponse.getAbort());
    }

    @Test
    void getFromResponseBody_Exception() {
        MappingContext ctx = new MappingContext();
        String body = "Invalid JSON";

        SerializationException exception = assertThrows(SerializationException.class, () -> {
            QueryResponse.getFromResponseBody(ctx, Deserializer.DYNAMIC, 200, body);
        });

        assertEquals("Error occurred while parsing the response body", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains(
            "Unrecognized token 'Invalid': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')"));
    }

}