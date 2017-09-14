package au.com.mountainpass.ryvr.testclient;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrCollectionImpl;
import au.com.mountainpass.ryvr.testclient.model.HtmlRyvrSource;

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
  public Set<Map.Entry<String, Ryvr>> entrySet() {
    ensureLoaded();
    List<WebElement> items = webDriver.findElements(By.cssSelector("a[rel~='item']"));
    Map<String, Ryvr> rval = new HashMap<>();
    for (WebElement item : items) {
      String title = item.getText().trim();
      rval.put(title, new Ryvr(title, 10, new HtmlRyvrSource(webDriver, item)));

    }
    return rval.entrySet();
  }

  private void ensureLoaded() {
    if (!loaded) {
      link.click();
      HtmlRyvrClient.waitTillLoaded(webDriver, 5);
      loaded = true;
    }
  }
}
