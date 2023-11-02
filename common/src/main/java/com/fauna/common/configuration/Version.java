package com.fauna.common.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Version class is a utility class that provides functionality to retrieve the software version.
 * It reads the version information from a properties file.
 */
public final class Version {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Version() {
    }

    /**
     * Retrieves the software version from the "version.properties" file.
     *
     * @return A String representing the software version.
     * @throws RuntimeException If the "version.properties" file is not found or if there is an issue reading the file.
     */
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
