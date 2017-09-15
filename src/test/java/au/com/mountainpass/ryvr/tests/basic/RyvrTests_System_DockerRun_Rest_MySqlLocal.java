package au.com.mountainpass.ryvr.tests.basic;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import au.com.mountainpass.SpringProfileCucumber;
import cucumber.api.CucumberOptions;

@RunWith(SpringProfileCucumber.class)
@CucumberOptions(plugin = { "pretty" }, features = {
    "src/test/resources/features/basic/"  }, strict = false, glue = {
        "au.com.mountainpass.ryvr.tests.basic.steps", "au.com.mountainpass.ryvr.tests.common.steps" }, tags = { "~@performance", "~@coverage" })
@ActiveProfiles({ "systemTest", "dockerRun", "restApi", "mysql" })
public class RyvrTests_System_DockerRun_Rest_MySqlLocal {

}
