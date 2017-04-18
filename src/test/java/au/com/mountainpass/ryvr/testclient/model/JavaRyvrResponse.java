package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import au.com.mountainpass.ryvr.model.Entry;
import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;

public class JavaRyvrResponse implements RyvrResponse {
    private Ryvr ryvr;
    private Long page;

    public JavaRyvrResponse(Ryvr ryvr) {
        this(ryvr, null);
    }

    public JavaRyvrResponse(Ryvr ryvr, Long page) {
        this.ryvr = ryvr;
        this.page = page;
    }

    @Override
    public void assertHasItem(List<Map<String, String>> events)
            throws URISyntaxException {
        ryvr.refresh(page);
        Embedded embedded = ryvr.getEmbedded();
        List<Entry> items = embedded.getItemsBy("item", Entry.class);
        assertThat(items.size(), equalTo(events.size()));
        for (int i = 0; i < items.size(); ++i) {
            final Map<String, String> expectedRow = events.get(i);
            items.get(i).getProperties().entrySet().forEach(entry -> {

                Object actualValue = entry.getValue();

                String expectedValue = expectedRow.get(entry.getKey());
                Util.assertEqual(actualValue, expectedValue);
            });
        }
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

    @Override
    public RyvrResponse followLink(String rel) throws URISyntaxException {
        ryvr.refresh(page);
        Optional<Link> prev = ryvr.getLinks().getLinkBy(rel);
        assertTrue(prev.isPresent());
        URI prevUri = URI.create(prev.get().getHref());
        List<NameValuePair> params = URLEncodedUtils.parse(prevUri,
                StandardCharsets.UTF_8);
        Optional<NameValuePair> pageNvp = params.stream()
                .filter(nvp -> "page".equals(nvp.getName())).findAny();
        assertTrue(pageNvp.isPresent());
        return new JavaRyvrResponse(ryvr, new Long(pageNvp.get().getValue()));

    }

    public Ryvr getRyvr() {
        return ryvr;
    }
}
