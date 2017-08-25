package au.com.mountainpass.ryvr.testclient;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;

public interface RyvrTestClient {

  public SwaggerResponse getApiDocs() throws Throwable;

  public RootResponse getRoot() throws Throwable;

  public RyvrsCollectionResponse getRyvrsCollection() throws Throwable;

  public Ryvr getRyvr(String name) throws Throwable;

  public void after(Scenario scenario);

  public void before(Scenario scenario);

  public Ryvr getRyvrDirect(String name) throws Throwable;

  public RyvrsCollectionResponse getRyvrsCollectionDirect() throws Throwable;

}
