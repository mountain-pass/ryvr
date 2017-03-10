package au.com.mountainpass.ryvr;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import au.com.mountainpass.inflector.springboot.InflectorApplication;
import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.testclient.RyvrTestClient;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
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

    @When("^a request is made for the API Docs$")
    public void a_request_is_made_for_the_API_Docs() throws Throwable {
        swaggerResponseFuture = client.getApiDocs();
    }

    @Then("^the API Docs will contain an operation for getting the API Docs$")
    public void the_API_Docs_will_contain_an_operation_for_getting_the_API_Docs()
            throws Throwable {
        swaggerResponseFuture.get().assertHasGetApiDocsOperation();

    }

}
