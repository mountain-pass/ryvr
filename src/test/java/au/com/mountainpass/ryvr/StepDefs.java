package au.com.mountainpass.ryvr;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import au.com.mountainpass.inflector.springboot.InflectorApplication;
import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.testclient.RyvrTestClient;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@ComponentScan(basePackages = { "au.com.mountainpass.ryvr" })
@ContextConfiguration(classes = { InflectorApplication.class,
        RyvrConfiguration.class }, loader = SpringApplicationContextLoader.class)
@SpringApplicationConfiguration(classes = { InflectorApplication.class,
        RyvrConfiguration.class })
@WebIntegrationTest({ "server.port=0" })
public class StepDefs {

    @Autowired
    private RyvrTestClient client;
    private CompletableFuture<SwaggerResponse> swaggerResponseFuture;
    private CompletableFuture<RootResponse> rootResponseFuture;
    private CompletableFuture<RyvrsCollectionResponse> ryvrsCollectionResponse;

    @When("^a request is made for the API Docs$")
    public void a_request_is_made_for_the_API_Docs() throws Throwable {
        swaggerResponseFuture = client.getApiDocs();
    }

    @Then("^the API Docs will contain an operation for getting the API Docs$")
    public void the_API_Docs_will_contain_an_operation_for_getting_the_API_Docs()
            throws Throwable {
        swaggerResponseFuture.get(5, TimeUnit.SECONDS)
                .assertHasGetApiDocsOperation();
    }

    @When("^a request is made to the server's base URL$")
    public void a_request_is_made_to_the_server_s_base_URL() throws Throwable {
        rootResponseFuture = client.getRoot();
    }

    @Then("^the root entity will contain a link to the api-docs$")
    public void the_root_entity_will_contain_a_link_to_the_api_docs()
            throws Throwable {
        rootResponseFuture.get(5, TimeUnit.SECONDS).assertHasApiDocsLink();
    }

    @Then("^the root entity will contain a link to the ryvrs$")
    public void the_root_entity_will_contain_a_link_to_the_ryvrs()
            throws Throwable {
        rootResponseFuture.get(5, TimeUnit.SECONDS).assertHasRyvrsLink();
    }

    @Then("^the root entity will have an application name of \"([^\"]*)\"$")
    public void the_root_entity_will_have_an_application_name_of(
            String applicationName) throws Throwable {
        rootResponseFuture.get(5, TimeUnit.SECONDS)
                .assertHasTitle(applicationName);
    }

    @Given("^there are no ryvrs configured$")
    public void there_are_no_ryvrs_configured() throws Throwable {
        // do nothing
    }

    @When("^the ryvrs list is retrieved$")
    public void the_ryvrs_list_is_retrieved() throws Throwable {
        ryvrsCollectionResponse = client.getRyvrsCollection();
    }

    @Then("^the ryvrs list will be empty$")
    public void the_ryvrs_list_will_be_empty() throws Throwable {
        ryvrsCollectionResponse.get(5, TimeUnit.SECONDS).assertIsEmpty();
    }

    @Then("^the count of ryvrs will be (\\d+)$")
    public void the_count_of_ryvrs_will_be(int count) throws Throwable {
        ryvrsCollectionResponse.get(5, TimeUnit.SECONDS).assertCount(count);
    }

}
