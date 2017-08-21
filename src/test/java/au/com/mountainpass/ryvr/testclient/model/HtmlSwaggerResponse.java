package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HtmlSwaggerResponse implements SwaggerResponse {

  private WebDriver webDriver;

  public HtmlSwaggerResponse(WebDriver webDriver) {
    this.webDriver = webDriver;
  }

  @Override
  public void assertHasGetApiDocsOperation() {
    List<WebElement> elements = webDriver.findElements(By.id("operations-system-getApiDocs"));
    assertThat(elements, not(empty()));
  }

}
