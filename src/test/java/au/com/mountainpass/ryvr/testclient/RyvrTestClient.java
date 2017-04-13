package au.com.mountainpass.ryvr.testclient;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.junit.runner.Result;

import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;

public interface RyvrTestClient {

    public SwaggerResponse getApiDocs()
            throws InterruptedException, ExecutionException, URISyntaxException;

    public RootResponse getRoot();

    public RyvrsCollectionResponse getRyvrsCollection();

    public RyvrResponse getRyvr(String name);

    public void after(Scenario scenario);

    public void afterSuite(Result result);

}
