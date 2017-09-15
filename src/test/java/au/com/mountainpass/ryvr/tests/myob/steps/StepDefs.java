
package au.com.mountainpass.ryvr.tests.myob.steps;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.mountainpass.ryvr.Application;
import au.com.mountainpass.ryvr.config.TestConfiguration;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;

@EnableAsync
@ContextConfiguration(classes = { Application.class, TestConfiguration.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class StepDefs {

  @Given("^a MYOB ryvr with the following configuration$")
  public void a_MYOB_ryvr_with_the_following_configuration() throws Throwable {
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException();
  }

  @Given("^the client is authenticated$")
  public void the_client_is_authenticated() throws Throwable {
    // Write code here that turns the phrase above into concrete actions
    throw new PendingException();
  }

}
