package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HtmlRyvrsCollectionResponse implements RyvrsCollectionResponse {

    private WebDriver webDriver;

    public HtmlRyvrsCollectionResponse(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    @Override
    public void assertIsEmpty() {
        assertThat(webDriver.findElement(By.id("_embedded"))
                .findElements(By.className("item")), empty());
    }

    @Override
    public void assertCount(int count) {
        assertThat(Integer.parseInt(
                webDriver.findElement(By.id("property::count")).getText()),
                equalTo(count));
    }

}
