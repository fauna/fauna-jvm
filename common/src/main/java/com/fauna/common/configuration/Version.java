package com.fauna.common.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Version {

    private Version() {
    }

    public static String getVersion() {
        Properties properties = new Properties();
        try (InputStream input = Version.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                throw new RuntimeException("Version not found");
            }
            properties.load(input);
            return properties.getProperty("version");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to retrieve version");
        }
    }

}
