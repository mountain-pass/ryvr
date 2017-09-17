
package au.com.mountainpass.ryvr.tests.myob.steps;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.mountainpass.ryvr.Application;
import au.com.mountainpass.ryvr.config.TestConfiguration;
import au.com.mountainpass.ryvr.testclient.RyvrTestServerAdminDriver;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;

@EnableAsync
@ContextConfiguration(classes = { Application.class, TestConfiguration.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MyobStepDefs {

  @Autowired
  private RyvrTestServerAdminDriver configClient;

  private String scenarioId;

  @Before
  public void _before(final Scenario scenario) {
    scenarioId = scenario.getId();
  }

  private String uniquifyRyvrName(String string) {
    return string + "-" + scenarioId.replace(";", "-");
  }

  @Given("^a MYOB ryvr with the following configuration$")
  public void a_MYOB_ryvr_with_the_following_configuration(Map<String, String> config)
      throws Throwable {
    Map<String, String> newConfig = new HashMap<>(config);
    newConfig.put("name", uniquifyRyvrName(newConfig.get("name")));
    configClient.createMyobRyvr(newConfig);
  }

}
