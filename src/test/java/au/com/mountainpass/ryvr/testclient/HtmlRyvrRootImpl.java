package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.client.ClientProtocolException;
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
    throw new NotImplementedException("TODO");
  }

  @Override
  public void login(String username, String password) throws ClientProtocolException, IOException {
    throw new NotImplementedException("TODO");
  }

  @Override
  public void logout() {
    throw new NotImplementedException("TODO");
  }

}
