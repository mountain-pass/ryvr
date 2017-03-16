package au.com.mountainpass.ryvr.testclient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.testclient.model.HtmlRootResponse;
import au.com.mountainpass.ryvr.testclient.model.HtmlSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
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
            waitTillLoaded(webDriver, 5, By.id("api_info"));
            return new HtmlSwaggerResponse(webDriver);
        });
    }

    @Override
    public CompletableFuture<RootResponse> getRoot() {
        return CompletableFuture.supplyAsync(() -> {
            URI url = config.getBaseUri().resolve("/");
            webDriver.get(url.toString());
            waitTillLoaded(webDriver, 5);
            return new HtmlRootResponse(webDriver);
        });
    }

    public static void waitTillLoaded(WebDriver webDriver,
            long timeoutInSeconds) {
        waitTillLoaded(webDriver, timeoutInSeconds, By.id("loaded"));
    }

    public static void waitTillLoaded(WebDriver webDriver,
            long timeoutInSeconds, By by) {
        (new WebDriverWait(webDriver, timeoutInSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    @Override
    public CompletableFuture<RyvrsCollectionResponse> getRyvrsCollection() {
        return getRoot()
                .thenCompose(rootResponse -> rootResponse.followRyvrsLink());
    }

}
