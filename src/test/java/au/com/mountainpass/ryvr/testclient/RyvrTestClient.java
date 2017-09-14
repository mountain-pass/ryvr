package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrRoot;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.model.SwaggerImpl;
import cucumber.api.Scenario;

public interface RyvrTestClient {

  public SwaggerImpl getApiDocs() throws Throwable;

  public RyvrRoot getRoot() throws Throwable;

  public RyvrsCollection getRyvrsCollection() throws Throwable;

  public Ryvr getRyvr(String name) throws Throwable;

  public void after(Scenario scenario) throws ClientProtocolException, IOException;

  public void before(Scenario scenario);

  public default Ryvr getRyvrDirect(String name) throws Throwable {
    return getRyvrDirect(name, 1);
  }

  public Ryvr getRyvrDirect(String name, int page) throws Throwable;

  public RyvrsCollection getRyvrsCollectionDirect() throws Throwable;

}
