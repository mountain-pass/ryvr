package au.com.mountainpass.ryvr.testclient;

import static de.otto.edison.hal.traverson.Traverson.*;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestTemplate;

import com.fasterxml.jackson.databind.node.ObjectNode;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.testclient.model.JavaSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RestRootResponse;
import au.com.mountainpass.ryvr.testclient.model.RestRyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;
import de.otto.edison.hal.EmbeddedTypeInfo;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.traverson.Traverson;
import io.swagger.parser.SwaggerParser;

public class RestRyvrClient implements RyvrTestClient {
    private Logger logger = LoggerFactory.getLogger(RestRyvrClient.class);

    @Autowired
    AsyncRestTemplate restTemplate;

    @Autowired
    RyvrConfiguration config;

    String lastResponse;

    SwaggerParser swaggerParser = new SwaggerParser();

    @Override
    public SwaggerResponse getApiDocs()
            throws InterruptedException, ExecutionException {
        URI url = config.getBaseUri().resolve("/api-docs");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT,
                "application/hal+json;q=1,application/json;q=0.8,*/*;q=0.1");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        CompletableFuture<ResponseEntity<ObjectNode>> rval = FutureConverter
                .convert(restTemplate.exchange(url, HttpMethod.GET,
                        requestEntity, ObjectNode.class));
        return new JavaSwaggerResponse(
                swaggerParser.parse(rval.get().getBody().toString()));
    }

    @Override
    public RootResponse getRoot() {
        URI url = config.getBaseUri().resolve("/");

        Traverson startedWith = traverson(this::httpGet)
                .startWith(url.toString());
        return new RestRootResponse(traverson(this::httpGet),
                startedWith.getCurrentContextUrl(),
                startedWith.getResourceAs(Root.class).get());
    }

    @Autowired
    private CloseableHttpAsyncClient httpAsyncClient;

    private String httpGet(final Link link) {
        try {
            final HttpGet httpget = new HttpGet(link.getHref());
            if (link.getType().isEmpty()) {
                httpget.addHeader("Accept", "application/hal+json");
            } else {
                httpget.addHeader("Accept", link.getType());
            }
            // httpAsyncClient.start();
            CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<HttpResponse>();
            httpAsyncClient.execute(httpget,
                    new FutureCallback<HttpResponse>() {

                        @Override
                        public void failed(Exception ex) {
                            logger.error(ex.getMessage(), ex);
                            completableFuture.completeExceptionally(ex);
                        }

                        @Override
                        public void completed(HttpResponse result) {
                            completableFuture.complete(result);
                        }

                        @Override
                        public void cancelled() {
                            completableFuture.cancel(true);
                        }
                    });
            // TODO create async traverson
            return completableFuture.thenApply(response -> {
                String rval = null;
                try {
                    rval = EntityUtils.toString(response.getEntity());
                    // httpAsyncClient.close();
                } catch (Exception e) {
                    lastResponse = e.getMessage();
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
                lastResponse = rval;
                return rval;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public RyvrsCollectionResponse getRyvrsCollection() {
        URI url = config.getBaseUri().resolve("/");

        Traverson followed = traverson(this::httpGet).startWith(url.toString())
                .follow(Root.RELS_RYVRS_COLLECTION);
        RyvrsCollection ryvrsCollection = followed
                .getResourceAs(RyvrsCollection.class,
                        EmbeddedTypeInfo.withEmbedded("item", Ryvr.class))
                .get();
        return new RestRyvrsCollectionResponse(traverson(this::httpGet),
                followed.getCurrentContextUrl(), ryvrsCollection);
    }

    @Override
    public RyvrResponse getRyvr(String name) {

        return getRyvrsCollection().followEmbeddedRyvrLink(name);
    }

    @Override
    public void after(Scenario scenario) {
        if (lastResponse != null) {
            scenario.embed(lastResponse.getBytes(), "application/json");
        }
    }
}
