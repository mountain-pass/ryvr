package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.mountainpass.ryvr.model.Ryvr;
import cucumber.api.PendingException;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Summary;

public class JavaRyvrResponse implements RestRyvr {
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

  // @Override
  // public void assertHasItems(List<Map<String, String>> events) throws Throwable {
  // Iterator<Record> actualIterator = ryvr.iterator();
  // Iterator<Map<String, String>> expectedIterator = events.iterator();
  // do {
  // Record actualRecord = actualIterator.next();
  // Map<String, String> expectedRecord = expectedIterator.next();
  // for (int i = 0; i < actualRecord.size(); ++i) {
  // Field actualField = actualRecord.getField(i);
  // Object actualValue = actualField.getValue();
  // String expectedValue = expectedRecord.get(actualField.getName());
  // Util.assertEqual(actualValue, expectedValue);
  // }
  // } while (actualIterator.hasNext() && expectedIterator.hasNext());
  // assertThat(actualIterator.hasNext(), equalTo(expectedIterator.hasNext()));
  // }

  @Override
  public void assertHasLinks(List<String> links) {
    throw new PendingException();
    // Set<String> rels = ryvr.getLinks().keySet();
    // links.forEach(item -> {
    // assertThat(rels, hasItem(item));
    // });
  }

  @Override
  public void assertDoesntHaveLinks(List<String> links) {
    throw new PendingException();
    // Set<String> rels = ryvr.getLinks().keySet();
    // links.forEach(item -> {
    // assertThat(rels, not(hasItem(item)));
    // });
  }

  // @Override
  // public RestRyvr followLink(String rel) {
  // switch (rel) {
  // case "prev":
  // ryvr.prev();
  // break;
  // case "next":
  // ryvr.next();
  // break;
  // case "first":
  // ryvr.first();
  // break;
  // case "last":
  // ryvr.last();
  // break;
  // case "current":
  // ryvr.current();
  // break;
  // case "self":
  // ryvr.self();
  // break;
  // default:
  // throw new NotImplementedException();
  // }
  // return this;
  // }

  public Ryvr getRyvr() {
    return ryvr;
  }

  @Override
  public void assertItemsHaveStructure(List<String> structure) throws URISyntaxException {
    String[] fieldNames = ryvr.getFieldNames();
    assertThat(fieldNames.length, equalTo(structure.size()));

    assertThat(fieldNames, arrayContainingInAnyOrder(structure.toArray()));
  }

  @Override
  public void retrieveAllEvents() throws Throwable {
    long before = System.currentTimeMillis();
    Summary.Timer requestTimer = requestLatency.startTimer();
    RestRyvr response = followLink("first");
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
    throw new PendingException();

    // return ryvr.getLinks().containsKey(rel);
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

  @Override
  public RestRyvr followLink(String rel) {
    throw new NotImplementedException("TODO");
  }

}
