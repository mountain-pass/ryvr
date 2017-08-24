package au.com.mountainpass.ryvr;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import au.com.mountainpass.SpringProfileCucumber;
import cucumber.api.CucumberOptions;

@RunWith(SpringProfileCucumber.class)
@CucumberOptions(plugin = { "pretty" }, features = {
    "src/test/resources/features/" }, strict = false, glue = {
        "au.com.mountainpass.ryvr" }, tags = { "@performance", "@current" })
@ActiveProfiles({ "integrationTest", "javaApi", "mysql", "performance" })
public class RyvrTests_Integration_Performance_Java_MySqlLocal_Current {

}