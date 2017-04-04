package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

public class JavaRyvrsCollectionResponse implements RyvrsCollectionResponse {

    private RyvrsCollection ryvrsCollection;

    public JavaRyvrsCollectionResponse(RyvrsCollection ryvrsCollection) {
        this.ryvrsCollection = ryvrsCollection;
    }

    @Override
    public void assertIsEmpty() {
        assertThat(ryvrsCollection.getEmbedded().getItemsBy("item", Ryvr.class),
                empty());

    }

    @Override
    public void assertCount(int count) {
        assertThat(ryvrsCollection.getCount(), equalTo(count));
    }

    @Override
    public void assertHasItem(List<String> names) {
        List<String> titles = ryvrsCollection.getLinks().getLinksBy("item")
                .stream().map(item -> item.getTitle())
                .collect(Collectors.toList());
        assertThat(titles, containsInAnyOrder(names.toArray()));
    }

    @Override
    public RyvrResponse followEmbeddedRyvrLink(String name) {
        Ryvr ryvr = ryvrsCollection.getRyvr(name);
        return new JavaRyvrResponse(ryvr);
    }

}
