package au.com.mountainpass.ryvr.testclient;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrCollectionImpl;

public class HtmlRyvrCollectionImpl extends AbstractMap<String, Ryvr>
    implements RyvrCollectionImpl {

  private WebDriver webDriver;
  private WebElement link;
  private boolean loaded;

  public HtmlRyvrCollectionImpl(WebDriver webDriver) {
    this.webDriver = webDriver;
    List<WebElement> items = webDriver.findElements(By.cssSelector("#links > li > a"));
    assertThat(items, not(empty()));
    this.link = items.stream().filter(item -> "Ryvrs".equals(item.getText())).findAny().get();
    this.loaded = false;

  }

  @Override
  public Set<java.util.Map.Entry<String, Ryvr>> entrySet() {
    throw new NotImplementedException("TODO");
  }

}
