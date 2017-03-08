package au.com.mountainpass.ryvr;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import au.com.mountainpass.inflector.springboot.InflectorApplication;
import au.com.mountainpass.ryvr.client.RyvrClient;
import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.swagger.models.Swagger;

@ComponentScan(basePackages = { "au.com.mountainpass.ryvr" })
@ContextConfiguration(classes = { InflectorApplication.class,
        RyvrConfiguration.class }, loader = SpringApplicationContextLoader.class)
@SpringApplicationConfiguration(classes = { InflectorApplication.class,
        RyvrConfiguration.class })
@WebIntegrationTest({ "server.port=0" })
public class StepDefs {

    @Autowired
    private RyvrClient client;
    private CompletableFuture<ResponseEntity<?>> result;

    @When("^a request is made for the API Docs as \"(.*?)\"$")
    public void a_request_is_made_for_the_API_Docs_as(String mediaType)
            throws Throwable {
        result = client.getApiDocs(MediaType.valueOf(mediaType));
    }

    @Then("^the API Docs for Ryvr will be returned$")
    public void the_API_Docs_for_Ryvr_will_be_returned() throws Throwable {
        assertThat(result.get().getBody(), instanceOf(Swagger.class));
    }

    @Then("^the API Docs will contain an operation for getting the API Docs$")
    public void the_API_Docs_will_contain_an_operation_for_getting_the_API_Docs()
            throws Throwable {
        Swagger swagger = (Swagger) result.get().getBody();
        assertThat(swagger.getPaths().entrySet().stream().map(entry -> {
            return entry.getValue().getGet().getOperationId();
        }).collect(Collectors.toList()), hasItem("getApiDocs"));
    }

}
