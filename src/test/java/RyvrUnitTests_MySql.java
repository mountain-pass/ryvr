
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import au.com.mountainpass.SpringProfileCucumber;
import cucumber.api.CucumberOptions;

@RunWith(SpringProfileCucumber.class)
@CucumberOptions(plugin = { "pretty" }, features = {
        "src/test/resources/features/" }, strict = false, glue = {
                "au.com.mountainpass.ryvr" }, tags = { "~@performance" })
@ActiveProfiles({ "unitTest", "mysql" })
public class RyvrUnitTests_MySql {

}