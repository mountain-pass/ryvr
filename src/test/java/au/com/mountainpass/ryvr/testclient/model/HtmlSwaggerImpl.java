package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import au.com.mountainpass.ryvr.model.SwaggerImpl;
import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;

public class HtmlSwaggerImpl implements SwaggerImpl {

  private WebDriver webDriver;
  private boolean loaded;
  private WebElement link;

  public HtmlSwaggerImpl(WebDriver webDriver) {
    this.webDriver = webDriver;
    List<WebElement> items = webDriver.findElements(By.cssSelector("#links > li > a"));
    assertThat(items, not(empty()));
    this.link = items.stream().filter(item -> "API Docs".equals(item.getText())).findAny().get();
    this.loaded = false;
  }

  @Override
  public boolean containsOperation(String string) {
    ensureLoaded();
    return !webDriver.findElements(By.id("operations-system-getApiDocs")).isEmpty();
  }

  private void ensureLoaded() {
    if (!loaded) {
      link.click();
      HtmlRyvrClient.waitTillLoaded(webDriver, 5);
      loaded = true;
    }
  }

}
