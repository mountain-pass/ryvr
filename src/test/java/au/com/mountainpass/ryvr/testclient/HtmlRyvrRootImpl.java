package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import au.com.mountainpass.ryvr.model.RyvrRootImpl;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.model.SwaggerImpl;
import au.com.mountainpass.ryvr.testclient.model.HtmlSwaggerImpl;

public class HtmlRyvrRootImpl implements RyvrRootImpl {

  private WebDriver webDriver;

  public HtmlRyvrRootImpl(WebDriver webDriver) {
    this.webDriver = webDriver;
  }

  @Override
  public SwaggerImpl getApiDocs() throws ClientProtocolException, IOException {
    return new HtmlSwaggerImpl(webDriver);
  }

  @Override
  public RyvrsCollection getRyvrsCollection() throws ClientProtocolException, IOException {
    return new RyvrsCollection(new HtmlRyvrCollectionImpl(webDriver));
  }

  @Override
  public void login(String username, String password) throws ClientProtocolException, IOException {
    webDriver.findElement(By.id("username")).sendKeys(username);
    webDriver.findElement(By.id("password")).sendKeys(password);
    webDriver
        .findElement(By.cssSelector(
            "body > div.container.main-content > section > div.jumbotron > div > form > button"))
        .click();
    HtmlRyvrClient.waitTillLoaded(webDriver, 5);
  }

  @Override
  public void logout() {
    webDriver.findElement(By.cssSelector("#logout")).click();
  }

}
