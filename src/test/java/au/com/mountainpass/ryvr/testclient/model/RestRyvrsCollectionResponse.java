package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import au.com.mountainpass.ryvr.model.Entry;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import de.otto.edison.hal.EmbeddedTypeInfo;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrsCollectionResponse implements RyvrsCollectionResponse {

    private RyvrsCollection ryvrsCollection;
    private Traverson traverson;
    private URL contextUrl;

    public RestRyvrsCollectionResponse(Traverson traverson, URL contextUrl,
            RyvrsCollection ryvrsCollection) {
        this.traverson = traverson;
        this.contextUrl = contextUrl;
        this.ryvrsCollection = ryvrsCollection;
    }

    @Override
    public void assertIsEmpty() {
        assertThat(ryvrsCollection.getEmbedded().getItemsBy("item"), empty());

    }

    @Override
    public void assertCount(int count) {
        assertThat(ryvrsCollection.getCount(), equalTo(count));
    }

    @Override
    public void assertHasItem(List<String> names) {
        List<Link> items = ryvrsCollection.getLinks().getLinksBy("item");
        List<String> titles = items.stream().map(item -> item.getTitle())
                .collect(Collectors.toList());
        assertThat(titles, containsInAnyOrder(names.toArray()));
    }

    @Override
    public CompletableFuture<RyvrResponse> followEmbeddedRyvrLink(String name) {
        return CompletableFuture.supplyAsync(() -> {
            Traverson followed = traverson
                    .startWith(contextUrl, ryvrsCollection)
                    .follow("item", hasName(name));
            EmbeddedTypeInfo embeddedTypeInfo = EmbeddedTypeInfo
                    .withEmbedded("item", Entry.class);
            Ryvr ryvr = followed.getResourceAs(Ryvr.class, embeddedTypeInfo)
                    .get();
            return new RestRyvrResponse(traverson,
                    followed.getCurrentContextUrl(), ryvr);
        });
    }

    private Predicate<Link> hasName(String name) {
        return itemLink -> {
            return name.equals(itemLink.getName());
        };
    }

}
