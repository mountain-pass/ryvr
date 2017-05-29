package au.com.mountainpass.ryvr;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import au.com.mountainpass.SpringProfileCucumber;
import cucumber.api.CucumberOptions;

@RunWith(SpringProfileCucumber.class)
@CucumberOptions(plugin = { "pretty" }, features = {
        "src/test/resources/features/" }, strict = false, glue = {
                "au.com.mountainpass.ryvr" }, tags = { "~@performance" })
@ActiveProfiles({ "integrationTest", "ui", "h2", "safariIos", "sauceLabs" })
public class RyvrTests_Integration_Ui_H2Local_SafariIosSauceLabs {

}