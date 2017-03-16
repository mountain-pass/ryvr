package au.com.mountainpass.ryvr.testclient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestTemplate;

import com.fasterxml.jackson.databind.node.ObjectNode;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.testclient.model.JavaSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RestRootResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import io.swagger.parser.SwaggerParser;

public class RestRyvrClient implements RyvrTestClient {

    @Autowired
    AsyncRestTemplate restTemplate;

    @Autowired
    RyvrConfiguration config;

    SwaggerParser swaggerParser = new SwaggerParser();

    @Override
    public CompletableFuture<SwaggerResponse> getApiDocs() {
        URI url = config.getBaseUri().resolve("/api-docs");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT,
                "application/hal+json;q=1,application/json;q=0.8,*/*;q=0.1");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        CompletableFuture<ResponseEntity<ObjectNode>> rval = FutureConverter
                .convert(restTemplate.exchange(url, HttpMethod.GET,
                        requestEntity, ObjectNode.class));
        return rval.thenApply(response -> {
            return new JavaSwaggerResponse(
                    swaggerParser.parse(response.getBody().toString()));
        });
    }

    @Override
    public CompletableFuture<RootResponse> getRoot() {
        URI url = config.getBaseUri().resolve("/");

        CompletableFuture<RootResponse> rval = FutureConverter
                .convert(restTemplate.getForEntity(url, Root.class))
                .thenApply(response -> new RestRootResponse(restTemplate,
                        config, response.getBody()));
        return rval;
    }

    @Override
    public CompletableFuture<RyvrsCollectionResponse> getRyvrsCollection() {
        CompletableFuture<RootResponse> rootFuture = getRoot();
        return rootFuture
                .thenCompose(rootResponse -> rootResponse.followRyvrsLink());
    }
}
