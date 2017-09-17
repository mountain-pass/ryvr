
package au.com.mountainpass.ryvr.tests.common.steps;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.testclient.RyvrTestClient;
import au.com.mountainpass.ryvr.testclient.RyvrTestServerAdminDriver;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class CommonStepDefs {

  @Autowired
  private RyvrTestClient client;

  @Autowired
  private RyvrTestServerAdminDriver configClient;

  @Autowired
  private CommonState commonState;

  private RyvrsCollection ryvrsCollection;

  @Before
  public void _before(final Scenario scenario) {
    commonState.setScenarioId(scenario.getId());
  }

  @After
  public void _after(final Scenario scenario) throws ClientProtocolException, IOException {
    client.logout();
  }

  @Given("^the client is authenticated$")
  public void the_client_is_authenticated() throws Throwable {
    configClient.ensureStarted();
    client.getRoot().login("user", "password");
  }

  @When("^the ryvrs list is retrieved$")
  public void theRyvrsListIsRetrieved() throws Throwable {
    configClient.ensureStarted();
    ryvrsCollection = client.getRyvrsCollection();
  }

  @Then("^the count of ryvrs will be (\\d+)$")
  public void theCountOfRyvrsWillBe(final int count) throws Throwable {
    assertThat(ryvrsCollection.size(), equalTo(count));
  }

  @When("^the ryvrs list is retrieved directly$")
  public void the_ryvrs_list_is_retrieved_directly() throws Throwable {
    configClient.ensureStarted();
    ryvrsCollection = client.getRyvrsCollectionDirect();
  }

  @Then("^the ryvrs list will be empty$")
  public void theRyvrsListWillBeEmpty() throws Throwable {
    assertThat(ryvrsCollection.entrySet(), empty());
  }

  @Then("^the ryvrs list will contain the following entries$")
  public void theRyvrsListWillContainTheFollowingEntries(final List<String> names)
      throws Throwable {
    Set<String> actualNames = ryvrsCollection.keySet();
    Set<String> expectedNames = commonState.uniquifyRyvrNames(names);
    assertTrue(actualNames.stream().allMatch(actualName -> {
      return expectedNames.contains(actualName);
    }));
    assertThat(actualNames.size(), equalTo(names.size()));

  }
}
