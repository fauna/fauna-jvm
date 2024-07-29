package com.fauna.env;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DriverEnvironmentTest {

    @Test
    public void testDriverEnvironment() {
        DriverEnvironment env = new DriverEnvironment(DriverEnvironment.JvmDriver.JAVA);
        String serialized = env.toString();

        // These assertions attempt to check that everything looks correct while passing on both developer
        // machines, and in the build environment (GitHub Actions).
        assertTrue(serialized.contains("env=unknown"));
        assertTrue(serialized.contains("runtime=java"));
        assertTrue(serialized.contains("os="));
        // For tests this is set in the file test/resources/version.properties
        assertTrue(serialized.contains("driver=1.0.0"));

    }
}
