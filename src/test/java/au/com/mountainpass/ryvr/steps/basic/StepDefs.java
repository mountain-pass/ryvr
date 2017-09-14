
package au.com.mountainpass.ryvr.steps.basic;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.client.ClientProtocolException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.mountainpass.ryvr.Application;
import au.com.mountainpass.ryvr.config.HttpThroughputCounter;
import au.com.mountainpass.ryvr.config.TestConfiguration;
import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrRoot;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.model.SwaggerImpl;
import au.com.mountainpass.ryvr.testclient.RyvrTestClient;
import au.com.mountainpass.ryvr.testclient.RyvrTestDbDriver;
import au.com.mountainpass.ryvr.testclient.RyvrTestServerAdminDriver;
import au.com.mountainpass.ryvr.testclient.model.Util;
import cucumber.api.PendingException;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Summary;

@EnableAsync
@ContextConfiguration(classes = { Application.class, TestConfiguration.class })
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class StepDefs {

  @Autowired
  private RyvrTestClient client;

  @Autowired
  private RyvrTestServerAdminDriver configClient;

  @Autowired
  private RyvrTestDbDriver dbClient;

  private RyvrRoot root;

  private Ryvr ryvr;

  private RyvrsCollection ryvrsCollection;

  private SwaggerImpl swaggerResponse;
  private String currentTable;

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private String databaseName;

  @Autowired(required = false)
  private HttpThroughputCounter httpThroughputCounter;

  private boolean addingRecords;

  private long actualCount;

  private long finishedAdding;

  private long finishedReading;

  private int expectedRecordCount = 0;

  private int consumers;

  private long recordCount;

  private String scenarioId;

  private Throwable error;

  private Record record;

  @Before
  public void _before(final Scenario scenario) {
    client.before(scenario);
    configClient._before(scenario);
    clearMetrics();
    scenarioId = scenario.getId();
  }

  @After
  public void _after(final Scenario scenario) throws ClientProtocolException, IOException {
    client.after(scenario);
    configClient._after(scenario);
  }

  @Given("^a database \"([^\"]*)\"$")
  public void aDatabase(final String dbName) throws Throwable {

    assertThat(dbClient.getCatalog(dbName), equalTo(dbName));
    databaseName = dbName;
  }

  @When("^a request is made for the API Docs$")
  public void aRequestIsMadeForTheAPIDocs() throws Throwable {
    configClient.ensureStarted();
    swaggerResponse = client.getRoot().getApiDocs();
  }

  @When("^a request is made to the server's base URL$")
  public void aRequestIsMadeToTheServersBaseURL() throws Throwable {
    configClient.ensureStarted();
    root = client.getRoot();
  }

  @Given("^a database ryvr with the following configuration$")
  public void a_database_ryvr_with_the_following_configuration(Map<String, String> config)
      throws Throwable {
    Map<String, String> newConfig = dbClient.adjustConfig(config);
    newConfig.put("name", uniquifyRyvrName(newConfig.get("name")));
    configClient.createDataSourceRyvr(newConfig);

    // assertThat(ryvrsCollection.getRyvrs().keySet(),
    // hasItem(config.get("name")));
    // Ryvr ryvr = ryvrsCollection.getRyvrs().get(config.get("name"));
    // assertThat(ryvr, instanceOf(DataSourceRyvr.class));
    // DataSourceRyvr dataSourceRyvr = (DataSourceRyvr) ryvr;
    // assertThat(dataSourceRyvr.getJdbcTemplate().getDataSource()
    // .getConnection().getCatalog(), equalTo(config.get("database")));
    // assertThat(dataSourceRyvr.getPageSize(),
    // equalTo(Long.parseLong(config.get("page size"))));
    // assertThat(dataSourceRyvr.getTable(), equalTo(config.get("table")));
    // assertThat(dataSourceRyvr.getOrderedBy(),
    // equalTo(config.get("ordered by")));
    //
    // ryvrsCollection.getRyvrs().clear();
    // ryvrsCollection.getRyvrs().put(config.get("name"), ryvr);
  }

  private String uniquifyRyvrName(String string) {
    return string + "-" + scenarioId.replace(";", "-");
  }

  @Given("^the \"([^\"]*)\" table has the following events$")
  public void theTableHasTheFollowingEvents(final String table,
      final List<Map<String, String>> events) throws Throwable {
    final String catalog = databaseName;
    dbClient.insertRows(catalog, table, events);
  }

  @Given("^the client is authenticated$")
  public void the_client_is_authenticated() throws Throwable {
    configClient.ensureStarted();
    client.getRoot().login("user", "password");
  }

  @Then("^it will contain$")
  public void itWillContain(final List<Map<String, String>> events) throws Throwable {
    Iterator<Record> actualIterator = ryvr.getSource().iterator();
    Iterator<Map<String, String>> expectedIterator = events.iterator();
    while (actualIterator.hasNext() && expectedIterator.hasNext()) {
      Record actualRecord = actualIterator.next();
      Map<String, String> expectedRecord = expectedIterator.next();
      for (int i = 0; i < actualRecord.size(); ++i) {
        Field actualField = actualRecord.getField(i);
        if (!"created".equals(actualField.getName())) {
          Object actualValue = actualField.getValue();
          String expectedValue = expectedRecord.get(actualField.getName().toLowerCase());
          Util.assertEqual("`" + actualField.getName() + "` should be " + expectedValue,
              actualValue, expectedValue);
        }
      }
    }
    assertThat("Neither iterator should haveNext()", actualIterator.hasNext(),
        equalTo(expectedIterator.hasNext()));
  }

  @Then("^the API Docs will contain an operation for getting the API Docs$")
  public void theAPIDocsWillContainAnPperationForGettingTheAPIDocs() throws Throwable {
    assertThat(swaggerResponse.containsOperation("getApiDocs"), notNullValue());
  }

  @Then("^the count of ryvrs will be (\\d+)$")
  public void theCountOfRyvrsWillBe(final int count) throws Throwable {
    assertThat(ryvrsCollection.size(), equalTo(count));
  }

  @Then("^the root entity will contain a link to the api-docs$")
  public void theRootEntityWillContainALinkToTheApiDocs() throws Throwable {
    assertThat(root.getApiDocs(), notNullValue());
  }

  @Then("^the root entity will contain a link to the ryvrs$")
  public void theRootEntityWillContainALinkToTheRyvrs() throws Throwable {
    assertThat(root.getRyvrsCollection(), notNullValue());
  }

  @Then("^the root entity will have an application name of \"([^\"]*)\"$")
  public void theRootEntityWillHaveAnApplicationNameOf(final String applicationName)
      throws Throwable {
    assertThat(root.getTitle(), equalTo(applicationName));
  }

  @When("^the \"([^\"]*)\" ryvr is retrieved$")
  public void theRyvrIsRetrieved(final String name) throws Throwable {
    if (ryvrsCollection == null) {
      theRyvrsListIsRetrieved();
    } else {
      configClient.ensureStarted();
    }
    ryvr = ryvrsCollection.get(uniquifyRyvrName(name));
  }

  @When("^(-?\\d+)th record of the \"([^\"]*)\" ryvr is retrieved$")
  public void th_record_of_the_ryvr_is_retrieved(int postion, String name) throws Throwable {
    theRyvrIsRetrieved(name);
    try {
      record = null;
      record = ryvr.getSource().iterator(postion).next();
    } catch (NoSuchElementException e) {
      error = e;
    }
  }

  @When("^(-?\\d+)th page of the \"([^\"]*)\" ryvr is retrieved$")
  public void th_page_of_the_ryvr_is_retrieved(int page, String name) throws Throwable {
    configClient.ensureStarted();
    try {
      ryvr = client.getRyvrDirect(uniquifyRyvrName(name), page);
    } catch (NoSuchElementException e) {
      error = e;
    }
  }

  @When("^the \"([^\"]*)\" rvyr is deleted$")
  public void the_rvyr_is_deleted(String name) throws Throwable {
    configClient.deleteRvyr(uniquifyRyvrName(name));
    // this results in a stale ryvrsCollection, which is what we want, because
    // we want to test what happens when we follow the link that doesn't exist anymore.
  }

  @When("^the \"([^\"]*)\" ryvr is retrieved directly$")
  public void the_ryvr_is_retrieved_directly(String name) throws Throwable {
    configClient.ensureStarted();
    ryvr = client.getRyvrDirect(uniquifyRyvrName(name));
  }

  @Then("^the ryvr will not be found$")
  public void the_ryvr_will_not_be_found() throws Throwable {
    assertThat(ryvr, nullValue());
  }

  @Then("^the page will not be found$")
  public void the_page_will_not_be_found() throws Throwable {
    assertThat(ryvr, nullValue());
  }

  @Then("^the record will not be found$")
  public void the_record_will_not_be_found() throws Throwable {
    assertThat(record, nullValue());
    assertThat(error, notNullValue());
    assertThat(error, instanceOf(NoSuchElementException.class));
  }

  @When("^the ryvrs list is retrieved$")
  public void theRyvrsListIsRetrieved() throws Throwable {
    configClient.ensureStarted();
    ryvrsCollection = client.getRyvrsCollection();
  }

  @When("^the ryvrs list is retrieved directly$")
  public void the_ryvrs_list_is_retrieved_directly() throws Throwable {
    configClient.ensureStarted();
    ryvrsCollection = client.getRyvrsCollectionDirect();
  }

  @Then("^the ryvrs list will be empty$")
  public void theRyvrsListWillBeEmpty() throws Throwable {
    assertThat(ryvrsCollection.entrySet(), empty());
  }

  @Then("^the ryvrs list will contain the following entries$")
  public void theRyvrsListWillContainTheFollowingEntries(final List<String> names)
      throws Throwable {
    Set<String> actualNames = ryvrsCollection.keySet();
    Set<String> expectedNames = uniquifyRyvrNames(names);
    assertTrue(actualNames.stream().allMatch(actualName -> {
      return expectedNames.contains(actualName);
    }));
    assertThat(actualNames.size(), equalTo(names.size()));

  }

  private Set<String> uniquifyRyvrNames(List<String> names) {
    return names.stream().map(name -> uniquifyRyvrName(name)).collect(Collectors.toSet());
  }

  @Given("^there are no ryvrs configured$")
  public void thereAreNoRyvrsConfigured() throws Throwable {
    configClient.clearRyvrs();
  }

  @Given("^it has a table \"([^\"]*)\" with the following structure$")
  public void itHasATableWithTheFollowingStructure(final String table,
      final Map<String, String> structure) throws Throwable {
    dbClient.createTable("test_db", table, structure);
    this.currentTable = table;
  }

  @Given("^it has (\\d+) events$")
  public void itHasEvents(int noOfEvents) throws Throwable {
    int batchSize = 8192;
    int batches = noOfEvents / batchSize;
    for (int batch = 0; batch <= batches; ++batch) {
      int eventsInBatch = batchSize;
      if (batch == batches) {
        eventsInBatch = noOfEvents % batchSize;
      }
      List<Map<String, String>> events = new ArrayList<>(eventsInBatch);
      for (int i = 0; i < eventsInBatch; ++i) {
        Map<String, String> event = new HashMap<>(4);
        event.put("id", Integer.toString(i + batch * batchSize));
        event.put("account", "78901234");
        event.put("description", "Buying Stuff");
        event.put("amount", Double.toString(i * -20.00 - (i + batch * batchSize)));
        event.put("created", Long.toString(System.currentTimeMillis()));
        events.add(event);
      }
      final String table = this.currentTable;
      final List<Map<String, String>> events1 = events;
      dbClient.insertRows("test_db", table, events1);
      int added = eventsInBatch + batch * batchSize;
      LOGGER.info("Added {} records of {}. {}%", added, noOfEvents,
          (added * 1.0) / noOfEvents * 100.0);
    }
    expectedRecordCount = noOfEvents;
  }

  @Then("^it will have the following structure$")
  public void itWillHaveTheFollowingStructure(List<String> structure) throws Throwable {
    ryvr.getFieldNames();
  }

  @Then("^it will have (\\d+) events$")
  public void itWillHaveTheEvents(long noOfEvents) throws Throwable {
    assertThat(ryvr.getSource().stream().count(), equalTo(noOfEvents));
    long actualCount = 0;
    Iterator<Record> iterator = ryvr.getSource().iterator();
    while (iterator.hasNext()) {
      ++actualCount;
      iterator.next();
    }
    assertThat(actualCount, equalTo(noOfEvents));
  }

  // static protected final Summary readLatencyMetrics = Summary.build().quantile(0.5, 0.01)
  // .quantile(0.95, 0.01).quantile(0.99, 0.01).quantile(1, 0.01).name("read_latency_seconds")
  // .help("Read latency in seconds.").register();

  @When("^all the events are retrieved$")
  public void all_the_events_are_retrieved() throws Throwable {
    consumers = 1;
    clearMetrics();
    System.gc();
    long before = System.nanoTime();
    recordCount = ryvr.getSource().stream().count();
    long after = System.nanoTime();
    double localLatencyµs = (after - before) / 1000.0;
    LOGGER.info("total latency: {}µs", localLatencyµs);
    LOGGER.info("MTPS(local): {}", recordCount / localLatencyµs);

    if (httpThroughputCounter != null) {
      double byteCount = httpThroughputCounter.getBytes();
      double latencySeconds = httpThroughputCounter.getTotalLatency();
      LOGGER.info("bytes: {}MB", byteCount / 1024.0 / 1024.0);
      LOGGER.info("throughput (local): {}MB/s",
          byteCount / 1024.0 / 1024.0 / localLatencyµs * 1000000);
      LOGGER.info("throughput: {}MB/s", byteCount / 1024.0 / 1024.0 / latencySeconds);
      LOGGER.info("total latency(prom): {}µs", latencySeconds * 1000000.0);
      LOGGER.info("MTPS(prom): {}", recordCount / latencySeconds / 1000000);
      httpThroughputCounter.logLatencies();
    }

  }

  @When("^all the events are retrieved by (\\d+) consumers$")
  public void all_the_events_are_retrieved_by_consumers(int consumers) throws Throwable {
    this.consumers = consumers;
    List<Ryvr> ryvrs = new ArrayList<>(consumers);
    for (int i = 0; i < consumers; ++i) {
      ryvrs.add(client.getRyvr(ryvr.getTitle()));
      if (i % 100 == 99) {
        LOGGER.info("created client: {}", i + 1);
      }
    }

    clearMetrics();
    System.gc();
    LOGGER.info("starting clients");
    executionTimes = ryvrs.parallelStream().mapToDouble(r -> {
      long b = System.nanoTime();
      recordCount = r.getSource().stream().count();
      long a = System.nanoTime();
      assertThat(recordCount, equalTo(100000L));
      return (a - b) / 1000.0 / 1000.0 / 1000.0;
    }).summaryStatistics();

    LOGGER.info("total latency min: {}s", executionTimes.getMin());
    LOGGER.info("total latency ave: {}s", executionTimes.getAverage());
    LOGGER.info("total latency max: {}s", executionTimes.getMax());
    LOGGER.info("MTPS(local  min): {}", recordCount / executionTimes.getMax() / 1000000.0);
    LOGGER.info("MTPS(local  ave): {}", recordCount / executionTimes.getAverage() / 1000000.0);
    LOGGER.info("MTPS(local  max): {}", recordCount / executionTimes.getMin() / 1000000.0);

    if (httpThroughputCounter != null) {

      double byteCount = httpThroughputCounter.getBytes();
      double latencySeconds = httpThroughputCounter.getTotalLatency();

      LOGGER.info("total latency(prom): {}s", latencySeconds / consumers);

      megaBytes = byteCount / 1024.0 / 1024.0;
      LOGGER.info("bytes total: {}MB", megaBytes);
      LOGGER.info("bytes/consumer : {}MB", megaBytes / consumers);
      LOGGER.info("throughput (local min): {}MB/s",
          megaBytes / executionTimes.getMax() / consumers);
      LOGGER.info("throughput (local ave): {}MB/s",
          megaBytes / executionTimes.getAverage() / consumers);
      LOGGER.info("throughput (local max): {}MB/s",
          megaBytes / executionTimes.getMin() / consumers);
      LOGGER.info("throughput (prom): {}MB/s", megaBytes / latencySeconds);

      LOGGER.info("MTPS(prom): {}", recordCount * consumers / latencySeconds / 1000000.0);

      httpThroughputCounter.logLatencies();
    }
  }

  @When("^all the events are retrieved again$")
  public void all_the_events_are_retrieved_again() throws Throwable {
    LOGGER.info("warm the cache");
    ryvr.getSource().stream().count();
    LOGGER.info("Clearing metrics");
    clearMetrics();
    all_the_events_are_retrieved();
  }

  private void clearMetrics() {
    if (httpThroughputCounter != null) {
      httpThroughputCounter.clear();
    }
  }

  public void assertLoadedWithin(int percentile, double maxMs) {
    if (httpThroughputCounter != null) {
      assertThat(httpThroughputCounter.getLatency(percentile) * 1000.0, lessThan(maxMs));
    }
  }

  @Then("^(\\d+)% of the pages should be loaded within (\\d+\\.?\\d*)ms$")
  public void of_the_pages_should_be_loaded_within_ms(int percentile, double maxMs)
      throws Throwable {
    assertLoadedWithin(percentile, maxMs);
  }

  @Then("^the average page should be loaded within (\\d+\\.?\\d*)ms$")
  public void the_average_page_should_be_loaded_within_ms(double maxMs) throws Throwable {
    assertLoadedWithin(50, maxMs);
  }

  @Then("^on the second retrieve, (\\d+)% of the pages should be loaded within (\\d+\\.?\\d*)ms$")
  public void on_the_second_retrieve_of_the_pages_should_be_loaded_within_ms(int percentile,
      double maxMs) throws Throwable {
    assertLoadedWithin(percentile, maxMs);
  }

  @Then("^on the second retrieve, the average page should be loaded within (\\d+\\.?\\d*)ms$")
  public void on_the_second_retrieve_the_average_page_should_be_loaded_within_ms(double maxMs)
      throws Throwable {
    assertLoadedWithin(50, maxMs);
  }

  @Then("^the event retrieval throughput should be at least (\\d+\\.?\\d*)MB/s$")
  public void the_event_retrieval_throughput_should_be_at_least_MB_s(double minMBps)
      throws Throwable {
    if (httpThroughputCounter != null) {

      double byteCount = httpThroughputCounter.getBytes();
      double latencySeconds = httpThroughputCounter.getTotalLatency();
      double megaBytes = byteCount / 1024.0 / 1024.0;
      assertThat(megaBytes / latencySeconds, greaterThan(minMBps));
    }
  }

  @Then("^the event retrieval rate should be at least (\\d+\\.?\\d*)TPS$")
  public void the_event_retrieval_rate_should_be_at_least_TPS(double minTps) throws Throwable {
    if (httpThroughputCounter != null) {
      double latencySeconds = httpThroughputCounter.getTotalLatency();
      assertThat(recordCount * consumers / latencySeconds, greaterThan(minTps));
    }
  }

  @Then("^the event retrieval rate should be at least (\\d+\\.?\\d*)MTPS$")
  public void the_event_retrieval_rate_should_be_at_least_MTPS(double minMTps) throws Throwable {
    if (httpThroughputCounter != null) {
      double latencySeconds = httpThroughputCounter.getTotalLatency();
      assertThat(recordCount * consumers / latencySeconds / 1000000.0, greaterThan(minMTps));
    }
  }

  @Then("^it will come from cache$")
  public void it_will_come_from_cache() throws Throwable {
    throw new PendingException();
    // ryvr.assertFromCache();
  }

  @Then("^it will not come from cache$")
  public void it_will_not_come_from_cache() throws Throwable {
    throw new PendingException();
  }

  static protected final Summary writeReadLatency = Summary.build().quantile(0.5, 0.01)
      .quantile(0.95, 0.01).quantile(0.99, 0.01).quantile(1, 0.01)
      .name("write_read_latency_seconds").help("Write Read latency in seconds.").register();

  static private double[] insertTimes;

  private DoubleSummaryStatistics executionTimes;

  private double megaBytes;

  @When("^(\\d+) records are added at a rate of (\\d+) records/s$")
  public void records_are_added_at_a_rate_of_records_s(int noOfEvents, int rate) throws Throwable {
    writeReadLatency.clear();
    addingRecords = true;
    int countSoFar = expectedRecordCount;
    expectedRecordCount += noOfEvents;
    insertTimes = new double[expectedRecordCount];

    long tick = 1000000000L / rate;

    CompletableFuture.runAsync(() -> {
      long start = System.nanoTime();
      for (int i = 0; i < noOfEvents; ++i) {
        Map<String, String> event = new HashMap<>(4);
        int id = i + countSoFar;
        event.put("id", Integer.toString(id));
        event.put("account", "78901234");
        event.put("description", "Buying Stuff");
        event.put("amount", Double.toString(i * -20.00 - i));
        try {
          insertTimes[id] = System.nanoTime();
          dbClient.insertRow("test_db", this.currentTable, event);

          // if (i % 100 == 99) {
          // LOGGER.info("added record: {}", id + 1);
          // }
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
        long next = start + tick * i;
        long now = System.nanoTime();
        long sleepFor = next - now;
        if (sleepFor > 0) {
          try {
            Thread.sleep(sleepFor / 1000000L, (int) (sleepFor % 1000000));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        } else {
          LOGGER.error("Falling behind ({}): {}ms", i, -sleepFor / 1000000L);
        }
      }
      finishedAdding = System.nanoTime();
      addingRecords = false;
      LOGGER.info("Finished adding records");
    });

  }

  @When("^all the records are retrieved while the records are added$")
  public void all_the_records_are_retrieved_while_the_records_are_added() throws Throwable {
    // Optional<Record> first = ryvr.getSource().stream().findFirst();
    // if (first.isPresent()) {
    // LOGGER.info("FIRST ID: {}", first.get().getField(0).getValue());
    // }
    actualCount = 0;
    // Optional<Record> last = ryvr.getSource().stream(actualCount - 1).findFirst();
    // if (last.isPresent()) {
    // LOGGER.info("LAST ID: {}", last.get().getField(0).getValue());
    // } else {
    // LOGGER.info("NO RECORD FOUND FOR POSTION: {}", actualCount - 1);
    // }
    finishedReading = System.nanoTime();
    while (actualCount < expectedRecordCount) {
      ryvr.getSource().refresh();
      // first = ryvr.getSource().stream(actualCount).findFirst();
      // if (first.isPresent()) {
      // LOGGER.info("FIRST ID: {}", first.get().getField(0).getValue());
      // }
      DoubleSummaryStatistics stats = ryvr.getSource().stream(actualCount).mapToDouble(record -> {
        long readTime = System.nanoTime();
        // Long created = (Long) record.getField(4).getValue();
        int id = (Integer) record.getField(0).getValue();
        double latency = (readTime - insertTimes[id]) / Collector.NANOSECONDS_PER_SECOND;
        writeReadLatency.observe(latency);
        LOGGER.info("latency {}: {}ms", id, latency * Collector.MILLISECONDS_PER_SECOND);
        return latency;
      }).summaryStatistics();
      long additionalCount = stats.getCount();
      actualCount += additionalCount;
      finishedReading = System.nanoTime();
      LOGGER.info("New Records: {}", additionalCount);
      LOGGER.info("Reached end: {}", actualCount);
      if (actualCount < expectedRecordCount) {
        Thread.sleep(100);
      }
    }

    List<MetricFamilySamples> results = writeReadLatency.collect();
    Stream<Sample> quantiles = results.get(0).samples.stream()
        .filter(sample -> sample.labelNames.contains("quantile"));
    quantiles.forEachOrdered(sample -> {
      LOGGER.info("latency - {}th percentile: {}ms",
          (int) (Double.parseDouble(sample.labelValues.get(0)) * 100),
          Math.round(sample.value * Collector.MILLISECONDS_PER_SECOND));
    });

  }

  @Then("^(\\d+) records will be retrieved$")
  public void records_will_be_retrieved(long count) throws Throwable {
    assertThat(actualCount, equalTo(count));
  }

  public double getWriteReadLatency(int percentile) {
    List<MetricFamilySamples> results = writeReadLatency.collect();
    String stringPercentile;
    switch (percentile) {
    case 50:
      stringPercentile = "0.5";
      break;
    case 95:
      stringPercentile = "0.95";
      break;
    case 99:
      stringPercentile = "0.99";
      break;
    case 100:
      stringPercentile = "1.0";
      break;
    default:
      throw new NotImplementedException("percentile not supported: " + percentile);
    }
    Stream<Sample> quantiles = results.get(0).samples.stream()
        .filter(sample -> sample.labelNames.contains("quantile"));
    Sample result = quantiles.filter(sample -> sample.labelValues.contains(stringPercentile))
        .findAny().get();
    return result.value;
  }

  public void assertWriteReadLatencyWithin(int percentile, double maxMs) {
    assertThat(getWriteReadLatency(percentile) * 1000.0, lessThan(maxMs));
  }

  @Then("^the average write-read latency should be less that (\\d+)ms$")
  public void the_average_write_read_latency_should_be_less_that_ms(int maxMs) throws Throwable {
    // Write code here that turns the phrase above into concrete actions
    assertWriteReadLatencyWithin(50, maxMs);
  }

  @Then("^write-read latency for (\\d+)% of the records should be less that (\\d+)ms$")
  public void write_read_latency_for_of_the_records_should_be_less_that_ms(int percentile,
      int maxMs) throws Throwable {
    assertWriteReadLatencyWithin(percentile, maxMs);
  }

  @Then("^the maximium write-read latency should be less that (\\d+\\.?\\d*)ms$")
  public void the_maximium_write_read_latency_should_be_less_that_ms(int maxMs) throws Throwable {
    assertWriteReadLatencyWithin(100, maxMs);
  }

  @Then("^the minmium event retrieval throughput should be at least (\\d+\\.?\\d*)MB/s$")
  public void the_minmium_event_retrieval_throughput_should_be_at_least_MB_s(double mbPerS)
      throws Throwable {
    assertThat(megaBytes / executionTimes.getMax() / consumers, greaterThan(mbPerS));
  }

  @Then("^the average event retrieval throughput should be at least (\\d+\\.?\\d*)MB/s$")
  public void the_average_event_retrieval_throughput_should_be_at_least_MB_s(double mbPerS)
      throws Throwable {
    assertThat(megaBytes / executionTimes.getAverage() / consumers, greaterThan(mbPerS));
  }

  @Then("^the peak event retrieval throughput should be at least (\\d+\\.?\\d*)GB/s$")
  public void the_peak_event_retrieval_throughput_should_be_at_least_GB_s(double gbPerS)
      throws Throwable {
    assertThat(megaBytes / 1024.0 / executionTimes.getMin() / consumers, greaterThan(gbPerS));
  }

  @Then("^the minimum event retrieval rate should be at least (\\d+\\.?\\d*)TPS$")
  public void the_minimum_event_retrieval_rate_should_be_at_least_MTPS(double tps)
      throws Throwable {
    assertThat(recordCount / executionTimes.getMax(), greaterThan(tps));
  }

  @Then("^the average event retrieval rate should be at least (\\d+\\.?\\d*)MTPS$")
  public void the_average_event_retrieval_rate_should_be_at_least_MTPS(double mtps)
      throws Throwable {
    assertThat(recordCount / executionTimes.getAverage() / 1000000.0, greaterThan(mtps));
  }

  @Then("^the peak event retrieval rate should be at least (\\d+\\.?\\d*)MTPS$")
  public void the_peak_event_retrieval_rate_should_be_at_least_MTPS(double mtps) throws Throwable {
    assertThat(recordCount / executionTimes.getMin() / 1000000.0, greaterThan(mtps));
  }

}
