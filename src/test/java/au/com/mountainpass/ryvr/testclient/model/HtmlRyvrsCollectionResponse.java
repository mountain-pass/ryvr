package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HtmlRyvrsCollectionResponse implements RyvrsCollectionResponse {

    private WebDriver webDriver;

    public HtmlRyvrsCollectionResponse(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    @Override
    public void assertIsEmpty() {
        assertThat(webDriver.findElements(By.id("items")), empty());
    }

    @Override
    public void assertCount(int count) {
        assertThat(Integer.parseInt(
                webDriver.findElement(By.id("property::count")).getText()),
                equalTo(count));
    }

    @Override
    public void assertHasEmbedded(List<String> names) {
        List<WebElement> items = webDriver.findElement(By.id("items"))
                .findElements(By.className("item"));
        List<String> itemNames = items.stream().map(item -> item.getText())
                .collect(Collectors.toList());
        assertThat(itemNames, containsInAnyOrder(names.toArray()));
    }

}
