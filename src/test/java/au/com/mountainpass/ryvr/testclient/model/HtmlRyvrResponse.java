package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HtmlRyvrResponse implements RyvrResponse {

    private WebDriver webDriver;

    public HtmlRyvrResponse(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    @Override
    public void assertHasItem(List<Map<String, String>> events) {
        List<String> headings = webDriver.findElement(By.id("linkedItems"))
                .findElements(By.className("linkedItemHeading")).stream()
                .map(heading -> heading.getText()).collect(Collectors.toList());
        List<WebElement> items = webDriver.findElement(By.id("linkedItems"))
                .findElements(By.className("linkedItemRow"));
        List<Map<String, Object>> itemValues = items.stream().map(item -> {
            Map<String, Object> rval = new HashMap<>();
            String id = item.getAttribute("id").replace("item:row:", "");
            for (int i = 0; i < headings.size(); ++i) {
                if (headings.get(i).trim().length() > 0) {
                    WebElement itemValueElement = item.findElement(
                            By.id("item:data:" + id + ":" + headings.get(i)));
                    String itemValue = itemValueElement.getText();
                    String itemType = itemValueElement.getAttribute("class");
                    if (java.util.Arrays.binarySearch(itemType.split("\\s"),
                            "number") >= 0) {
                        rval.put(headings.get(i).trim(),
                                new BigDecimal(itemValue));
                    } else {
                        rval.put(headings.get(i).trim(), itemValue);
                    }
                }
            }
            return rval;
        }).collect(Collectors.toList());

        for (int i = 0; i < itemValues.size(); ++i) {
            final Map<String, String> expectedRow = events.get(i);
            itemValues.get(i).entrySet().forEach(entry -> {

                Object actualValue = entry.getValue();

                String expectedValue = expectedRow.get(entry.getKey());
                Util.assertEqual(actualValue, expectedValue);
            });
        }
        assertThat(items.size(), equalTo(events.size()));
    }

    @Override
    public void assertHasLinks(List<String> links) {
        links.forEach(rel -> {
            webDriver.findElement(By.cssSelector("a[rel~='" + rel + "']"));
        });
    }

    @Override
    public void assertDoesntHaveLinks(List<String> links) {
        links.forEach(rel -> {
            assertThat(webDriver.findElements(
                    By.cssSelector("a[rel~='" + rel + "']")), empty());
        });
    }

}
