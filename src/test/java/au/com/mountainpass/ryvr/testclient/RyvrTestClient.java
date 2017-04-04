package au.com.mountainpass.ryvr.testclient;

import java.util.concurrent.CompletableFuture;

import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;

public interface RyvrTestClient {

    public CompletableFuture<SwaggerResponse> getApiDocs();

    public CompletableFuture<RootResponse> getRoot();

    public CompletableFuture<RyvrsCollectionResponse> getRyvrsCollection();

    public CompletableFuture<RyvrResponse> getRyvr(String name);

    public void after(Scenario scenario);

}
