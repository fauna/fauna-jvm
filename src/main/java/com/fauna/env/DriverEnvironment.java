package com.fauna.common.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DriverEnvironment {

    private final String driverVersion;
    private final String env;
    private final String os;
    private String runtime;

    public enum JvmDriver {
        JAVA, SCALA
    }

    public DriverEnvironment(JvmDriver jvmDriver) {
        this.env = getRuntimeEnvironment();
        this.os = System.getProperty("os.name") + "-" + System.getProperty("os.version");
        this.runtime = String.format("java-%s", System.getProperty("java.version"));
        this.driverVersion = getVersion();
        if (jvmDriver == JvmDriver.SCALA) {
            this.runtime = String.format("%s,scala", this.runtime);
        }
    }

    /**
     * Retrieves the software version from the "version.properties" file.
     *
     * @return A String representing the software version.
     * @throws RuntimeException If the "version.properties" file is not found or if there is an issue reading the file.
     */
    public static String getVersion() {
        Properties properties = new Properties();
        try (InputStream input = DriverEnvironment.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                throw new RuntimeException("Version not found.");
            }
            properties.load(input);
            return properties.getProperty("version");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to retrieve version.");
        }
    }

    private String getRuntimeEnvironment() {
        // Checks for various cloud environments based on environment variables
        if (System.getenv("PATH") != null && System.getenv("PATH").contains(".heroku")) {
            return "Heroku";
        } else if (System.getenv("AWS_LAMBDA_FUNCTION_VERSION") != null) {
            return "AWS Lambda";
        } else if (System.getenv("_") != null && System.getenv("_").contains("google")) {
            return "GCP Cloud Functions";
        } else if (System.getenv("GOOGLE_CLOUD_PROJECT") != null) {
            return "GCP Compute Instances";
        } else if (System.getenv("WEBSITE_FUNCTIONS_AZUREMONITOR_CATEGORIES") != null) {
            return "Azure Cloud Functions";
        } else if (System.getenv("ORYX_ENV_TYPE") != null && System.getenv("WEBSITE_INSTANCE_ID") != null &&
                System.getenv("ORYX_ENV_TYPE").equals("AppService")) {
            return "Azure Compute";
        } else {
            return "Unknown";
        }
    }

    @Override
    public String toString() {
        return String.format("driver=%s; runtime=java-%s; env=%s; os=%s",
                driverVersion, runtime, env, os).toLowerCase();
    }

}
