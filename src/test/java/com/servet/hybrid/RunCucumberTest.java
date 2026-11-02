package com.servet.hybrid;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** Entry point for mvn test: every .feature in the package "features". */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
class RunCucumberTest {
}
