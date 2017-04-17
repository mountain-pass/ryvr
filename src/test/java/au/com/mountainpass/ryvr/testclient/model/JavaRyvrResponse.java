package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.mountainpass.ryvr.model.Entry;
import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.Embedded;

public class JavaRyvrResponse implements RyvrResponse {
    private Ryvr ryvr;

    public JavaRyvrResponse(Ryvr ryvr) {
        this.ryvr = ryvr;
    }

    @Override
    public void assertHasItem(List<Map<String, String>> events)
            throws URISyntaxException {
        ryvr.refresh();
        Embedded embedded = ryvr.getEmbedded();
        List<Entry> items = embedded.getItemsBy("item", Entry.class);
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
