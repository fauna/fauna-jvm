package com.fauna.client;

import com.fauna.common.configuration.FaunaConfig;
import com.fauna.common.configuration.FaunaEnvironment;
import com.fauna.common.connection.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FaunaClientTest {

    @Mock
    private Connection connection;

    private FaunaClient defaultClient;

    @BeforeEach
    void setUp() {
        defaultClient = new FaunaClient();
    }

    @Test
    void defaultConfigBuilder() {
        FaunaConfig config = FaunaConfig.builder().build();
        assertEquals("https://db.fauna.com", config.getEndpoint());
        assertEquals("", config.getSecret());
        // The default values are never null.
        assertEquals(Optional.empty(), config.getLinearized());
        assertEquals(Optional.empty(), config.getTypeCheck());
        assertEquals(Optional.empty(), config.getTraceParent());
        assertEquals(new HashMap<>(), config.getQueryTags());
        assertEquals(Duration.ofSeconds(5), config.getQueryTimeout());
    }

    @Test
    void customConfigBuilder() {
        FaunaConfig config = FaunaConfig.builder()
                .endpoint("endpoint")
                .secret("secret")
                .linearized(false)
                .typeCheck(false)
                .traceParent("parent")
                .queryTags(Map.of("t1", "v1", "t2", "v2"))
                .queryTimeout(Duration.ofMinutes(1))
                .build();

        assertEquals("endpoint", config.getEndpoint());
        assertEquals("secret", config.getSecret());
        assertEquals(Optional.of(false), config.getLinearized());
        assertEquals(Optional.of(false), config.getTypeCheck());
        assertEquals(Optional.of("parent"), config.getTraceParent());
        assertEquals(Set.of("t1", "t2"), config.getQueryTags().keySet());
        assertEquals(Duration.ofSeconds(60), config.getQueryTimeout());

    }

    @Test
    void defaltClientConstructor() {
        FaunaClient client = new FaunaClient();
        assertTrue(client.toString().startsWith("com.fauna.client.FaunaClient"));
    }

    @Test
    void customClientConstructor() {
        FaunaConfig config = FaunaConfig.builder().queryTimeout(Duration.ofSeconds(30)).build();
        FaunaClient client = new FaunaClient(config);
        assertTrue(client.toString().startsWith("com.fauna.client.FaunaClient"));
    }

    @Test()
    void environmentVarConfigConstructor() {
        // Note that the secret passed in through the builder is overridden by the FAUNA_* environment variables.
        try (MockedStatic<FaunaEnvironment> env = Mockito.mockStatic(FaunaEnvironment.class)) {
            env.when(FaunaEnvironment::faunaSecret).thenReturn(Optional.of("secret"));
            env.when(FaunaEnvironment::faunaEndpoint).thenReturn(Optional.of("endpoint"));
            FaunaConfig faunaConfig = FaunaConfig.builder().secret("overridden").endpoint("overridden").build();
            assertEquals("secret", faunaConfig.getSecret());
            assertEquals("endpoint", faunaConfig.getEndpoint());
        }
    }

    @Test
    void emptyEnvironmentVarConfigConstructor() {
        try (MockedStatic<FaunaEnvironment> env = Mockito.mockStatic(FaunaEnvironment.class)) {
            env.when(FaunaEnvironment::faunaSecret).thenReturn(Optional.empty());
            env.when(FaunaEnvironment::faunaEndpoint).thenReturn(Optional.empty());
            FaunaConfig.Builder builder = FaunaConfig.builder().secret("secret").endpoint("endpoint");
            FaunaConfig faunaConf = builder.build();
            assertEquals("secret", faunaConf.getSecret());
            assertEquals("endpoint", faunaConf.getEndpoint());
        }
    }

    @Test
    void nullConfigClientConstructor() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new FaunaClient(null),
                "null FaunaConfig should throw"
        );
        assertEquals("FaunaConfig cannot be null.", thrown.getMessage() );
    }

    @Test
    void query_WhenFqlIsNull_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> defaultClient.query(null),
                "Expected query() to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("The provided FQL query is null."));
    }

}