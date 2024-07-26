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
import com.fauna.beans.Person;
import com.fauna.codec.Codec;
import com.fauna.codec.CodecProvider;
import com.fauna.constants.ResponseFields;
import com.fauna.exception.ClientException;
import com.fauna.exception.ProtocolException;
import com.fauna.mapping.MappingContext;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Optional;

import com.fauna.serialization.Serializer;
import com.fauna.types.Document;
import org.junit.jupiter.api.Test;

class QueryResponseTest {
    MappingContext context = new MappingContext();
    Codec<Document> docCodec = CodecProvider.generate(context, Document.class);

    @Test
    public void getFromResponseBody_Success() throws IOException {
        Person baz = new Person("baz", "luhrman", 'A', 64);
        String data = Serializer.serialize(baz);
        String body = "{\"stats\":{},\"static_type\":\"Person\",\"data\":" + data + "}";
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn(body);
        when(resp.statusCode()).thenReturn(200);

        QuerySuccess<Person> success = QueryResponse.handleResponse(resp, CodecProvider.generate(context, Person.class));

        assertEquals(baz.getFirstName(), success.getData().getFirstName());
        assertEquals("Person", success.getStaticType().get());


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
        String body = "{\"not valid json\"";
        when(resp.statusCode()).thenReturn(400);
        when(resp.body()).thenReturn(body);

        ProtocolException exc = assertThrows(ProtocolException.class, () -> QueryResponse.handleResponse(resp, docCodec));
        assertEquals("ProtocolException HTTP 400 with body: " + body, exc.getMessage());
        assertEquals(400, exc.getStatusCode());
        assertEquals(body, exc.getBody());
    }

    @Test
    public void handleResponseWithMissingStatsThrowsProtocolException() {
        HttpResponse resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn("{\"not valid json\"");
        assertThrows(ProtocolException.class, () -> QueryResponse.handleResponse(resp, docCodec));
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