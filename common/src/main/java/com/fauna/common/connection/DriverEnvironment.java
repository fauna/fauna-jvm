package com.fauna.common.connection;

class DriverEnvironment {

    private final String javaVersion;
    private final String driverVersion = "1.0";
    private final String env;
    private final String os;

    public DriverEnvironment() {
        this.javaVersion = System.getProperty("java.version");
        this.env = getRuntimeEnvironment();
        this.os = System.getProperty("os.name") + "-" + System.getProperty("os.version");
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
        return String.format("driver=java-%s; runtime=java-%s; env=%s; os=%s",
                driverVersion, javaVersion, env, os).toLowerCase();
    }

}
