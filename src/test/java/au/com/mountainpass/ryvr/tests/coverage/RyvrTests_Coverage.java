package au.com.mountainpass.ryvr.tests.coverage;

import org.junit.runner.RunWith;

import au.com.mountainpass.SpringProfileCucumber;
import cucumber.api.CucumberOptions;

@RunWith(SpringProfileCucumber.class)
@CucumberOptions(plugin = { "pretty" }, features = {
    "src/test/resources/features/coverage/" }, strict = false, glue = {
        "au.com.mountainpass.ryvr.tests.coverage.steps" }, tags = { "@coverage" })
public class RyvrTests_Coverage {

}
