package com.fauna.common.configuration;

import com.fauna.common.connection.DriverEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionTest {

    @Test
    void getVersion_shouldReturnCorrectVersion() {
        String version = DriverEnvironment.getVersion();

        assertEquals("1.0.0", version);
    }

}