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
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Summary;

public class JavaRyvrResponse implements RyvrResponse {
    private Ryvr ryvr;
    private Long page;
    static final Summary requestLatency = Summary.build().quantile(0.95, 0.01)
            .quantile(1, 0.01).name("requests_latency_seconds")
            .help("Request latency in seconds.").register();

    public JavaRyvrResponse(Ryvr ryvr) {
        this(ryvr, null);
    }

    public JavaRyvrResponse(Ryvr ryvr, Long page) {
        this.ryvr = ryvr;
        this.page = page;
    }

    @Override
    public void assertHasItems(List<Map<String, String>> events)
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
        Optional<Link> link = ryvr.getLinks().getLinkBy(rel);
        assertTrue(link.isPresent());
        URI linkUri = URI.create(link.get().getHref());
        List<NameValuePair> params = URLEncodedUtils.parse(linkUri,
                StandardCharsets.UTF_8);
        Optional<NameValuePair> pageNvp = params.stream()
                .filter(nvp -> "page".equals(nvp.getName())).findAny();
        if (pageNvp.isPresent()) {
            return new JavaRyvrResponse(ryvr,
                    new Long(pageNvp.get().getValue()));
        } else {
            return new JavaRyvrResponse(ryvr);
        }
    }

    public Ryvr getRyvr() {
        return ryvr;
    }

    @Override
    public void assertItemsHaveStructure(List<String> structure)
            throws URISyntaxException {
        ryvr.refresh(page);
        Embedded embedded = ryvr.getEmbedded();
        List<Entry> items = embedded.getItemsBy("item", Entry.class);
        assertThat(items, not(empty()));

        assertThat(items.get(0).getProperties().keySet(),
                containsInAnyOrder(structure.toArray()));
    }

    void processNextRequest(String rel) {

        // requestLatency.time(new Runnable() {
        // RyvrResponse rval;
        //
        // @Override
        // public void run() {
        // try {
        // followLink("next");
        // } catch (URISyntaxException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        // });
    }

    @Override
    public void retrieveAllEvents() throws URISyntaxException {
        Summary.Timer requestTimer = requestLatency.startTimer();
        RyvrResponse response = followLink("first");
        requestTimer.observeDuration();

        while (response.hasLink("next")) {
            requestTimer = requestLatency.startTimer();
            response = response.followLink("next");
            requestTimer.observeDuration();
        }
    }

    @Override
    public boolean hasLink(String rel) {
        return ryvr.getLinks().getLinkBy(rel).isPresent();
    }

    @Override
    public void assertLoadedWithin(int percentile, int maxMs) {
        List<MetricFamilySamples> results = requestLatency.collect();
        String stringPercentile = percentile == 95 ? "0.95" : "1.0";
        Sample result = results.get(0).samples.stream()
                .filter(sample -> sample.labelNames.contains("quantile")
                        && sample.labelValues.contains(stringPercentile))
                .findAny().get();
        assertThat(result.value * 1000.0, lessThan(maxMs * 1.0));
    }

}
