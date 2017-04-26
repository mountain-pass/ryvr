package au.com.mountainpass.ryvr.testclient;

import static de.otto.edison.hal.traverson.Traverson.*;

import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.testclient.model.JavaSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RestRootResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.traverson.Traverson;
import io.swagger.parser.SwaggerParser;

public class RestRyvrClient implements RyvrTestClient {
    private Logger logger = LoggerFactory.getLogger(RestRyvrClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RyvrConfiguration config;

    private String lastResponse;

    private SwaggerParser swaggerParser = new SwaggerParser();

    @Override
    public SwaggerResponse getApiDocs() throws InterruptedException,
            ExecutionException, URISyntaxException {

        Traverson traverson = getTraverson();
        HalRepresentation root = traverson.getResource().get();
        String docsHref = root.getLinks().getLinkBy("describedby").get()
                .getHref();
        Link docsLink = Link.link("describedby", traverson
                .getCurrentContextUrl().toURI().resolve(docsHref).toString());
        String swagger = httpGet(docsLink);

        return new JavaSwaggerResponse(swaggerParser.parse(swagger));
    }

    @Override
    public RootResponse getRoot() {
        Traverson startedWith = getTraverson();
        return new RestRootResponse(traverson(this::httpGet),
                startedWith.getCurrentContextUrl(),
                startedWith.getResourceAs(Root.class).get(), restTemplate);
    }

    private Traverson getTraverson() {
        URI url = config.getBaseUri().resolve("/");

        Traverson startedWith = traverson(this::httpGet)
                .startWith(url.toString());
        return startedWith;
    }

    @Autowired
    private CloseableHttpAsyncClient httpAsyncClient;

    public String httpGet(final Link link) {
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
        return getRoot().followRyvrsLink();
    }

    @Override
    public RyvrResponse getRyvr(String name) {
        return getRyvrsCollection().followRyvrLink(name);
    }

    @Override
    public void after(Scenario scenario) {
        if (lastResponse != null) {
            scenario.embed(lastResponse.getBytes(), "application/json");
        }
    }

    @Override
    public void before(Scenario scenario) {
        // nothing
    }
}
