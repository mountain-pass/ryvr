package au.com.mountainpass.ryvr.testclient;

import java.util.concurrent.CompletableFuture;

import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;

public interface RyvrTestClient {

    public CompletableFuture<SwaggerResponse> getApiDocs();

    public CompletableFuture<RootResponse> getRoot();

    public CompletableFuture<RyvrsCollectionResponse> getRyvrsCollection();

}
