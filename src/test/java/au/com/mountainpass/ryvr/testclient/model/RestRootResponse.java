package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URL;

import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import de.otto.edison.hal.traverson.Traverson;

public class RestRootResponse implements RootResponse {

    private Root root;
    private Traverson traverson;
    private URL contextUrl;

    public RestRootResponse(Traverson traverson, URL contextUrl, Root root) {
        this.traverson = traverson;
        this.contextUrl = contextUrl;
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
    public RyvrsCollectionResponse followRyvrsLink() {
        Traverson followed = traverson.startWith(contextUrl, root)
                .follow("https://ryvr.io/rels/ryvrs-collection");
        RyvrsCollection ryvrsCollection = followed
                .getResourceAs(RyvrsCollection.class).get();
        return new RestRyvrsCollectionResponse(traverson,
                followed.getCurrentContextUrl(), ryvrsCollection);
    }

}
