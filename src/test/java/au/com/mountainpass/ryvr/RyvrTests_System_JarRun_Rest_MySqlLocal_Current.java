package au.com.mountainpass.ryvr;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import au.com.mountainpass.SpringProfileCucumber;
import cucumber.api.CucumberOptions;

@RunWith(SpringProfileCucumber.class)
@CucumberOptions(plugin = { "pretty" }, features = {
    "src/test/resources/features/" }, strict = false, glue = {
        "au.com.mountainpass.ryvr.steps.main" }, tags = { "@current", "~@performance", "~@coverage" })
@ActiveProfiles({ "systemTest", "jarRun", "restApi", "mysql" })
public class RyvrTests_System_JarRun_Rest_MySqlLocal_Current {

}
