package com.fauna.common.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionTest {

    @Test
    void getVersion_shouldReturnCorrectVersion() {
        String version = Version.getVersion();

        assertEquals("1.0.0", version);
    }

}