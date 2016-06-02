package org.cf.trade.acceptance;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
/**
 * Cukes tests runner. 
 * 
 * NOTE: should be run against a *newly started* server, to prevent old data from interfering with the test assertions
 * */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty"})
public class RunCukesTest {
		
}
