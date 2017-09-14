package au.com.mountainpass.ryvr.tests.basic;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import au.com.mountainpass.SpringProfileCucumber;
import cucumber.api.CucumberOptions;

@RunWith(SpringProfileCucumber.class)
@CucumberOptions(plugin = { "pretty" }, features = {
    "src/test/resources/features/basic/"  }, strict = false, glue = {
        "au.com.mountainpass.ryvr.steps.basic" }, tags = { "~@performance", "~@coverage" })
@ActiveProfiles({ "systemTest", "distZipRun", "ui", "mysql", "chrome", "sauceLabs" })
public class RyvrTests_System_DistZipRun_Ui_MySqlLocal_ChromeSauceLabs {

}