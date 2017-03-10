package au.com.mountainpass.ryvr.testclient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.testclient.model.HtmlSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;

public class HtmlRyvrClient implements RyvrTestClient {

    @Autowired
    private WebDriver webDriver;

    @Autowired
    RyvrConfiguration config;

    @Override
    public CompletableFuture<SwaggerResponse> getApiDocs() {
        return CompletableFuture.supplyAsync(() -> {
            URI url = config.getBaseUri().resolve("/api-docs");
            webDriver.get(url.toString());
            return new HtmlSwaggerResponse(webDriver);
        });
    }

}
