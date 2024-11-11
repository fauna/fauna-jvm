/**
 * Provides utilities for managing the environment in which the Fauna driver operates.
 *
 * <p>This package includes classes that detect and encapsulate runtime information about the 
 * environment, operating system, JVM version, and driver version. These details can be used for 
 * diagnostics, compatibility checks, and logging purposes.</p>
 *
 * <ul>
 *   <li>{@link com.fauna.env.DriverEnvironment} - Detects and captures information about the runtime 
 *       environment, such as operating system details, Java version, and cloud environment (e.g., 
 *       AWS Lambda, GCP Cloud Functions).</li>
 * </ul>
 *
 * <p>The {@code com.fauna.env} package supports gathering environment-related details to help 
 * understand the conditions under which the driver is operating, which can be useful for 
 * troubleshooting and optimizing performance across various deployment platforms.</p>
 */
package com.fauna.env;
