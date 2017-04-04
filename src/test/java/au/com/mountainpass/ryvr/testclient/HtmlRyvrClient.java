package au.com.mountainpass.ryvr.testclient;

import java.net.URI;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.testclient.model.HtmlRootResponse;
import au.com.mountainpass.ryvr.testclient.model.HtmlSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;

public class HtmlRyvrClient implements RyvrTestClient {

    @Autowired
    private WebDriver webDriver;

    @Autowired
    RyvrConfiguration config;

    @Override
    public SwaggerResponse getApiDocs() {
        URI url = config.getBaseUri().resolve("/api-docs");
        webDriver.get(url.toString());
        waitTillLoaded(webDriver, 5, By.id("api_info"));
        return new HtmlSwaggerResponse(webDriver);
    }

    @Override
    public RootResponse getRoot() {
        URI url = config.getBaseUri().resolve("/");
        webDriver.get(url.toString());
        waitTillLoaded(webDriver, 5);
        return new HtmlRootResponse(webDriver);
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
    public RyvrsCollectionResponse getRyvrsCollection() {
        return getRoot().followRyvrsLink();
    }

    @Override
    public RyvrResponse getRyvr(String name) {
        return getRyvrsCollection().followEmbeddedRyvrLink(name);

    }

    @Override
    public void after(Scenario scenario) {
        if (webDriver instanceof TakesScreenshot) {
            byte[] screenshot = ((TakesScreenshot) webDriver)
                    .getScreenshotAs(OutputType.BYTES);
            scenario.embed(screenshot, "image/png");
        } else {
            scenario.embed(webDriver.getPageSource().getBytes(), "text/html");
        }
    }

}
