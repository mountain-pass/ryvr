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

import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;
import cucumber.api.PendingException;

public class HtmlRyvrResponse implements RyvrResponse {

  private WebDriver webDriver;

  public HtmlRyvrResponse(WebDriver webDriver) {
    this.webDriver = webDriver;
  }

  @Override
  public void assertHasItems(List<Map<String, String>> events) {
    HtmlRyvrClient.waitTillLoaded(webDriver, 5);
    List<WebElement> items = webDriver.findElement(By.id("items"))
        .findElements(By.className("itemRow"));

    List<Map<String, Object>> itemValues = items.stream().map(itemRow -> {
      Map<String, Object> rval = new HashMap<>();

      List<WebElement> fields = itemRow.findElements(By.tagName("td"));
      for (WebElement field : fields) {
        String heading = field.getAttribute("data-heading");
        String value = field.getText();
        String type = field.getAttribute("data-type");
        if ("number".equals(type)) {
          rval.put(heading, new BigDecimal(value));
        } else {
          rval.put(heading, value);
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
      assertThat(webDriver.findElements(By.cssSelector("a[rel~='" + rel + "']")), empty());
    });
  }

  @Override
  public RyvrResponse followLink(String rel) {

    WebElement link = webDriver.findElement(By.tagName("section"))
        .findElement(By.cssSelector("a[rel~='" + rel + "']"));
    link.click();
    HtmlRyvrClient.waitTillLoaded(webDriver, 5);
    return new HtmlRyvrResponse(webDriver);

  }

  @Override
  public void assertItemsHaveStructure(List<String> structure) {
    HtmlRyvrClient.waitTillLoaded(webDriver, 5);
    List<String> headings = webDriver.findElements(By.className("itemHeading")).stream()
        .map(element -> element.getText()).collect(Collectors.toList());
    assertThat(headings, containsInAnyOrder(structure.toArray()));
  }

  @Override
  public void retrieveAllEvents() {
    throw new PendingException();
  }

  @Override
  public boolean hasLink(String rel) {
    throw new PendingException();

  }

  @Override
  public void assertLoadedWithin(int percentile, int maxMs) {
    throw new PendingException();
  }

  @Override
  public void clearMetrics() {
    throw new PendingException();
  }

  @Override
  public void assertFromCache() {
    // TODO: add something to the page to indicate if the response was
    // cached or not
    throw new PendingException();
  }

  @Override
  public void assertNotFromCache() {
    // TODO: add something to the page to indicate if the response was
    // cached or not
    throw new PendingException();
  }

}
