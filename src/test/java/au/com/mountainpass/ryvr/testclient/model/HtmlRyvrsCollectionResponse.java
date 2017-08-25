package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;

public class HtmlRyvrsCollectionResponse implements RyvrsCollectionResponse {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private WebDriver webDriver;

  public HtmlRyvrsCollectionResponse(WebDriver webDriver) {
    this.webDriver = webDriver;
  }

  @Override
  public void assertIsEmpty() {
    assertThat(webDriver.findElements(By.id("linkedItems")), empty());
  }

  @Override
  public void assertCount(int count) {
    assertThat(webDriver.findElements(By.cssSelector("#linkedItems > ul > li > a")).size(),
        equalTo(count));
  }

  @Override
  public void assertHasItem(List<String> names) {
    List<WebElement> items = webDriver.findElements(By.cssSelector("#linkedItems > ul > li > a"));
    List<String> itemNames = items.stream().map(item -> {
      String text = item.getText();
      LOGGER.info("element: {}", item);
      LOGGER.info("element text: {}", text);
      return text;
    }).collect(Collectors.toList());
    assertThat(itemNames, containsInAnyOrder(names.toArray()));
  }

  @Override
  public Ryvr followRyvrLink(final String name) {
    HtmlRyvrClient.waitTillLoaded(webDriver, 5);
    List<WebElement> items = webDriver.findElements(By.cssSelector("a[rel~='item']"));
    assertThat(items, not(empty()));
    WebElement link = items.stream().filter(item -> name.equals(item.getText())).findAny().get();
    link.click();
    HtmlRyvrClient.waitTillLoaded(webDriver, 5);
    WebElement title = webDriver.findElement(By.cssSelector("body > div > section > div > h1"));
    if ("Tumbleweeds blow past".equals(title.getText())) {
      throw new NoSuchElementException("No value present");
    }
    return new Ryvr(name, 10, new HtmlRyvrSource(webDriver));
  }

  @Override
  public URL getContextUrl() {
    try {
      return new URL(webDriver.getCurrentUrl());
    } catch (MalformedURLException e) {
      throw new NotImplementedException(e);
    }
  }

}
