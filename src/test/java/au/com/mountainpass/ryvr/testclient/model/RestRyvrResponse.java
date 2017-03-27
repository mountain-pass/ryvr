package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.mountainpass.ryvr.model.Entry;
import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrResponse implements RyvrResponse {

    private Ryvr ryvr;
    private Traverson traverson;
    private URL contextUrl;

    public RestRyvrResponse(Traverson traverson, URL contextUrl, Ryvr ryvr) {
        this.traverson = traverson;
        this.contextUrl = contextUrl;
        this.ryvr = ryvr;
    }

    @Override
    public void assertHasEmbedded(List<Map<String, String>> events) {
        List<Entry> items = ryvr.getEmbedded().getItemsBy("item", Entry.class);
        for (int i = 0; i < items.size(); ++i) {
            final Map<String, String> expectedRow = events.get(i);
            items.get(i).getProperties().entrySet().forEach(entry -> {

                Object actualValue = entry.getValue();

                String expectedValue = expectedRow.get(entry.getKey());
                Util.assertEqual(actualValue, expectedValue);
            });
        }
        assertThat(items.size(), equalTo(events.size()));
    }

    @Override
    public void assertHasLinks(List<String> links) {
        Set<String> rels = ryvr.getLinks().getRels();
        links.forEach(item -> {
            assertThat(rels, hasItem(item));
        });
    }

    @Override
    public void assertDoesntHaveLinks(List<String> links) {
        Set<String> rels = ryvr.getLinks().getRels();
        links.forEach(item -> {
            assertThat(rels, not(hasItem(item)));
        });
    }

}
