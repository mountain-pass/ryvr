package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;

public class HtmlSwaggerResponse implements SwaggerResponse {

    private WebDriver webDriver;

    public HtmlSwaggerResponse(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    @Override
    public void assertHasGetApiDocsOperation() {
        HtmlRyvrClient.waitTillLoaded(webDriver, 5, By.id("api_info"));
        List<WebElement> elements = webDriver
                .findElements(By.id("system_getApiDocs"));
        assertThat(elements, not(empty()));
    }

}
