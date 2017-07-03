package au.com.mountainpass.ryvr.testclient;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;

public interface RyvrTestClient {

  public SwaggerResponse getApiDocs()
      throws InterruptedException, ExecutionException, URISyntaxException, MalformedURLException;

  public RootResponse getRoot() throws MalformedURLException;

  public RyvrsCollectionResponse getRyvrsCollection() throws MalformedURLException;

  public Ryvr getRyvr(String name) throws MalformedURLException;

  public void after(Scenario scenario);

  public void before(Scenario scenario);

}
