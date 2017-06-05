package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.mountainpass.ryvr.model.Ryvr;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Summary;

public class JavaRyvrResponse implements RyvrResponse {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private Ryvr ryvr;

  static protected final Summary requestLatency = Summary.build().quantile(0.5, 0.01)
      .quantile(0.95, 0.01).quantile(1, 0.01).name("requests_latency_seconds")
      .help("Request latency in seconds.").register();
  static protected final Summary receivedBytes = Summary.build().name("requests_size_bytes")
      .help("Request size in bytes.").register();

  public JavaRyvrResponse(Ryvr ryvr) {
    this(ryvr, null);
  }

  public JavaRyvrResponse(Ryvr ryvr, Long page) {
    this.ryvr = ryvr;
  }

  @Override
  public void assertHasItems(List<Map<String, String>> events) throws URISyntaxException {
    // ryvr.refresh(page);
    List<Map<String, Object>> rows = ryvr.getEmbedded().get("item");
    assertThat(rows.size(), equalTo(events.size()));
    for (int i = 0; i < rows.size(); ++i) {
      final Map<String, String> expectedRow = events.get(i);
      final Map<String, Object> row = rows.get(i);
      row.keySet().forEach(key -> {

        Object actualValue = row.get(key);

        String expectedValue = expectedRow.get(key);
        Util.assertEqual(actualValue, expectedValue);
      });
    }
  }

  @Override
  public void assertHasLinks(List<String> links) {
    Set<String> rels = ryvr.getLinks().keySet();
    links.forEach(item -> {
      assertThat(rels, hasItem(item));
    });
  }

  @Override
  public void assertDoesntHaveLinks(List<String> links) {
    Set<String> rels = ryvr.getLinks().keySet();
    links.forEach(item -> {
      assertThat(rels, not(hasItem(item)));
    });
  }

  @Override
  public RyvrResponse followLink(String rel) {
    switch (rel) {
    case "prev":
      ryvr.prev();
      break;
    case "next":
      ryvr.next();
      break;
    case "first":
      ryvr.first();
      break;
    case "last":
      ryvr.last();
      break;
    case "current":
      ryvr.current();
      break;
    case "self":
      ryvr.self();
      break;
    default:
      throw new NotImplementedException();
    }
    return this;
  }

  public Ryvr getRyvr() {
    return ryvr;
  }

  @Override
  public void assertItemsHaveStructure(List<String> structure) throws URISyntaxException {
    List<Map<String, Object>> items = ryvr.getEmbedded().get("item");
    assertThat(items, not(empty()));

    assertThat(items.get(0).keySet(), containsInAnyOrder(structure.toArray()));
  }

  @Override
  public void retrieveAllEvents() throws Throwable {
    long before = System.currentTimeMillis();
    Summary.Timer requestTimer = requestLatency.startTimer();
    RyvrResponse response = followLink("first");
    requestTimer.observeDuration();

    while (response.hasLink("next")) {
      requestTimer = requestLatency.startTimer();
      response = response.followLink("next");
      // don't count the last page, because it needs to re-query the DB
      if (response.hasLink("next")) {
        requestTimer.observeDuration();
      }
    }
    long after = System.currentTimeMillis();
    LOGGER.info("total latency: {}s", (after - before) / 1000.0);

    double byteCount = receivedBytes.collect().get(0).samples.stream()
        .filter(sample -> "requests_size_bytes_sum".equals(sample.name)).findAny().get().value;
    double latencySeconds = requestLatency.collect().get(0).samples.stream()
        .filter(sample -> "requests_latency_seconds_sum".equals(sample.name)).findAny().get().value;
    LOGGER.info("bytes: {}B", byteCount);
    LOGGER.info("bytes: {}KB", byteCount / 1024.0);
    LOGGER.info("bytes: {}MB", byteCount / 1024.0 / 1024.0);
    LOGGER.info("throughput: {}B/s", byteCount / latencySeconds);
    LOGGER.info("throughput: {}KB/s", byteCount / 1024.0 / latencySeconds);
    LOGGER.info("throughput: {}MB/s", byteCount / 1024.0 / 1024.0 / latencySeconds);
    LOGGER.info("total latency: {}s", latencySeconds);

    List<MetricFamilySamples> results = requestLatency.collect();
    Stream<Sample> quantiles = results.get(0).samples.stream()
        .filter(sample -> sample.labelNames.contains("quantile"));
    quantiles.forEachOrdered(sample -> {
      LOGGER.info("latency - {}th percentile: {}ms",
          (int) (Double.parseDouble(sample.labelValues.get(0)) * 100), sample.value * 1000);
    });
  }

  @Override
  public boolean hasLink(String rel) {
    return ryvr.getLinks().containsKey(rel);
  }

  @Override
  public void assertLoadedWithin(int percentile, int maxMs) {
    List<MetricFamilySamples> results = requestLatency.collect();
    String stringPercentile = percentile == 95 ? "0.95" : "1.0";
    Stream<Sample> quantiles = results.get(0).samples.stream()
        .filter(sample -> sample.labelNames.contains("quantile"));
    Sample result = quantiles.filter(sample -> sample.labelValues.contains(stringPercentile))
        .findAny().get();
    assertThat(result.value * 1000.0, lessThan(maxMs * 1.0));
  }

  @Override
  public void clearMetrics() {
    requestLatency.clear();
    receivedBytes.clear();
  }

  @Override
  public void assertFromCache() {
    // do nothing as caching is implemented at the REST layer
  }

  @Override
  public void assertNotFromCache() {
    // do nothing as caching is implemented at the REST layer
  }

}
