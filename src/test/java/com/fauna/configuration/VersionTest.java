package com.fauna.configuration;

import com.fauna.env.DriverEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionTest {

    @Test
    void getVersion_shouldReturnCorrectVersion() {
        String version = DriverEnvironment.getVersion();
        assertTrue(version.contains("0.1"));
    }

}
