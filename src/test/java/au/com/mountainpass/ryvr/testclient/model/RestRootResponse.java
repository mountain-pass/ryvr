package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import de.otto.edison.hal.traverson.Traverson;

public class RestRootResponse implements RootResponse {

    private Root root;
    private Traverson traverson;
    private URL contextUrl;
    private RestTemplate restTemplate;

    public RestRootResponse(Traverson traverson, URL contextUrl, Root root,
            RestTemplate restTemplate) {
        this.traverson = traverson;
        this.contextUrl = contextUrl;
        this.root = root;
        this.restTemplate = restTemplate;
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
    public RyvrsCollectionResponse followRyvrsLink() {
        try {
            String path = root.getLinks()
                    .getLinkBy(
                            "https://mountain-pass.github.io/ryvr/rels/ryvrs-collection")
                    .get().getHref();
            URI ryvrsUri = contextUrl.toURI().resolve(path);
            RyvrsCollection ryvrsCollection = restTemplate
                    .getForEntity(ryvrsUri, RyvrsCollection.class).getBody();
            return new RestRyvrsCollectionResponse(traverson, ryvrsUri.toURL(),
                    ryvrsCollection, restTemplate);
        } catch (MalformedURLException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new NotImplementedException();
        }
    }

}
