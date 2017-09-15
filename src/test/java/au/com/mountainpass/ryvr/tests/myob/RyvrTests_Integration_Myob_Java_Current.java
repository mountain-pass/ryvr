package au.com.mountainpass.ryvr.tests.myob;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import au.com.mountainpass.SpringProfileCucumber;
import cucumber.api.CucumberOptions;

@RunWith(SpringProfileCucumber.class)
@CucumberOptions(plugin = { "pretty" }, features = {
    "src/test/resources/features/myob" }, strict = false, glue = {
        "au.com.mountainpass.ryvr.tests.myob.steps",
        "au.com.mountainpass.ryvr.steps.common" }, tags = { "@current", "~@performance",
            "~@coverage" })
@ActiveProfiles({ "integrationTest", "javaApi", "myob" })
public class RyvrTests_Integration_Myob_Java_Current {
}
