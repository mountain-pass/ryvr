package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestTemplate;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.testclient.FutureConverter;
import de.otto.edison.hal.Link;

public class RestRootResponse implements RootResponse {

    private AsyncRestTemplate restTemplate;
    private Root root;
    private RyvrConfiguration config;

    public RestRootResponse(AsyncRestTemplate restTemplate,
            RyvrConfiguration config, Root root) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.root = root;
    }

    @Override
    public void assertHasApiDocsLink() {
        RootUtil.assertHasLink(root, "API Docs");
    }

    @Override
    public void assertHasRyvrsLink() {
        RootUtil.assertHasLink(root, "Ryvrs");
    }

    @Override
    public void assertHasTitle(String title) {
        assertThat(root.getTitle(), equalTo(title));
    }

    @Override
    public CompletableFuture<RyvrsCollectionResponse> followRyvrsLink() {
        Optional<Link> ryvrsCollectionLink = root.getLinks()
                .getLinkBy("https://ryvr.io/rels/ryvrs-collection");

        URI url = config.getBaseUri()
                .resolve(ryvrsCollectionLink.get().getHref());
        CompletableFuture<ResponseEntity<RyvrsCollection>> rval = FutureConverter
                .convert(restTemplate.getForEntity(url, RyvrsCollection.class));

        return rval.thenApply(ryvrsCollectionResponse -> {
            RyvrsCollection body = ryvrsCollectionResponse.getBody();
            return new RestRyvrsCollectionResponse(restTemplate, config,
                    body);
        });

    }

}
