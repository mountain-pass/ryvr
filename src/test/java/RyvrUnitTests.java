
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import au.com.mountainpass.SpringProfileCucumber;
import au.com.mountainpass.ryvr.testclient.RyvrTestClient;
import cucumber.api.CucumberOptions;

@RunWith(SpringProfileCucumber.class)
@CucumberOptions(plugin = { "pretty" }, features = {
        "src/test/resources/features/" }, strict = false, glue = {
                "au.com.mountainpass.ryvr" }, tags = {})
@ActiveProfiles("unitTest")
public class RyvrUnitTests {

    @Autowired
    private RyvrTestClient client;

}
