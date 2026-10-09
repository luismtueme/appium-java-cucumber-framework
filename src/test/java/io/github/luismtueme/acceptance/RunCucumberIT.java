package io.github.luismtueme.acceptance;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runs every feature under {@code src/test/resources/features} on the JUnit Platform. Settings are in
 * {@code junit-platform.properties}; {@code ./mvnw verify} runs it through the failsafe plugin.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.glue", value = "io.github.luismtueme.acceptance")
public class RunCucumberIT {}
