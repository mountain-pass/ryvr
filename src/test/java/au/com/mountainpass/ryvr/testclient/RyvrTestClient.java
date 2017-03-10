package au.com.mountainpass.ryvr.testclient;

import java.util.concurrent.CompletableFuture;

import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;

public interface RyvrTestClient {

    public CompletableFuture<SwaggerResponse> getApiDocs();

}
