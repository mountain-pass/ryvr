package au.com.mountainpass.ryvr.testclient;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.mountainpass.SauceLabsTunnel;
import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.testclient.model.HtmlRootResponse;
import au.com.mountainpass.ryvr.testclient.model.HtmlRyvrSource;
import au.com.mountainpass.ryvr.testclient.model.HtmlRyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.HtmlSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;

public class HtmlRyvrClient implements RyvrTestClient {
  private final static Logger LOGGER = LoggerFactory.getLogger(HtmlRyvrClient.class);

  @Autowired
  private WebDriver webDriver;

  @Autowired(required = false)
  SauceLabsTunnel sauceLabsTunnel;

  @Autowired
  private RyvrConfiguration config;

  private boolean failed = false;

  @Override
  public SwaggerResponse getApiDocs() {
    URI url = config.getBaseUri().resolve("/api-docs");
    webDriver.get(url.toString());
    waitTillLoaded(webDriver, 5,
        ExpectedConditions.visibilityOfElementLocated(By.id("operations-tag-system")));
    return new HtmlSwaggerResponse(webDriver);
  }

  @Override
  public RootResponse getRoot() {
    URI url = config.getBaseUri().resolve("/");
    webDriver.get(url.toString());
    waitTillLoaded(webDriver, 5);
    return new HtmlRootResponse(webDriver);
  }

  public static void waitTillLoaded(WebDriver webDriver, long timeoutInSeconds) {
    waitTillLoaded(webDriver, timeoutInSeconds,
        ExpectedConditions.and(ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")),
            ExpectedConditions.invisibilityOfElementLocated(By.id("loader"))));
    waitTillLoaded(webDriver, timeoutInSeconds, ExpectedConditions
        .visibilityOfElementLocated(By.cssSelector("body > div.container.main-content")));

  }

  public static void waitTillLoaded(WebDriver webDriver, long timeoutInSeconds,
      ExpectedCondition<?> expectedCondition) {

    (new WebDriverWait(webDriver, timeoutInSeconds)).until(expectedCondition);
  }

  @Override
  public RyvrsCollectionResponse getRyvrsCollection() {
    return getRoot().followRyvrsLink();
  }

  @Override
  public Ryvr getRyvr(String name) {
    return getRyvrsCollection().followRyvrLink(name);
  }

  @Override
  public void after(Scenario scenario) {
    assumeTrue(webDriver instanceof TakesScreenshot);
    byte[] screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
    scenario.embed(screenshot, "image/png");
    String status = scenario.getStatus();
    if (sauceLabsTunnel != null) {
      ((JavascriptExecutor) webDriver)
          .executeScript("sauce:context=" + scenario.getName() + ":" + status);
    }

    if (!failed) {
      switch (status) {
      case "failed":
        failed = true;
        // pass down
      case "passed":
        if (sauceLabsTunnel != null) {
          ((JavascriptExecutor) webDriver).executeScript("sauce:job-result=" + status);
        }
        break;
      default:
        // no nothing
      }
    }
  }

  static public List<WebElement> getLinks(WebDriver webDriver) {
    waitTillLoaded(webDriver, 5, ExpectedConditions.visibilityOfElementLocated(By.id("links")));
    List<WebElement> menuButton = webDriver
        .findElements(By.cssSelector("button.navbar-toggle.collapsed"));
    assertThat(menuButton.size(), lessThanOrEqualTo(1));
    menuButton.forEach(button -> {
      if (button.isDisplayed()) {
        button.click();
        String target = button.getAttribute("data-target");
        (new WebDriverWait(webDriver, 5))
            .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(target)));
      }
    });

    WebElement links = webDriver.findElement(By.id("links"));
    List<WebElement> linksList = links.findElements(By.tagName("a"));
    return linksList;
  }

  @Override
  public void before(Scenario scenario) {
    if (sauceLabsTunnel != null) {
      ((JavascriptExecutor) webDriver)
          .executeScript("sauce:context=" + scenario.getName() + ":started");
    }

  }

  public static void waitTillVisible(WebDriver webDriver, long timeoutInSeconds, String id) {
    LOGGER.info("waiting till {} loaded...", id);
    HtmlRyvrClient.waitTillLoaded(webDriver, timeoutInSeconds,
        ExpectedConditions.visibilityOfElementLocated(By.id(id)));
    LOGGER.info("...loaded", id);
  }

  @Override
  public Ryvr getRyvrDirect(String name, int page) throws Throwable {
    // instead of following the links, we are going to just construct the
    // URL and hit it directly, to ensure the correct 404 is returned
    URL contextUrl = getRyvrsCollection().getContextUrl();
    URI ryvrUri = contextUrl.toURI().resolve("/ryvrs/" + name + "?page=" + page);
    webDriver.get(ryvrUri.toString());
    HtmlRyvrClient.waitTillLoaded(webDriver, 5);
    WebElement title = webDriver.findElement(By.cssSelector("body > div > section > div > h1"));
    if ("Tumbleweeds blow past".equals(title.getText())) {
      throw new NoSuchElementException("No value present");
    }
    return new Ryvr(name, 10, new HtmlRyvrSource(webDriver));
  }

  @Override
  public RyvrsCollectionResponse getRyvrsCollectionDirect() throws Throwable {
    webDriver.get(config.getBaseUri().resolve("/ryvrs").toString());
    HtmlRyvrClient.waitTillLoaded(webDriver, 5);
    return new HtmlRyvrsCollectionResponse(webDriver);
  }

}
