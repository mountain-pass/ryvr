package au.com.mountainpass.ryvr.config;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Summary;
import io.prometheus.client.Summary.Timer;

@Component
@Profile(value = { "restApi" })
public class HttpThroughputCounter implements HttpResponseInterceptor, HttpRequestInterceptor {
  private static final String TIMER = "TIMER";

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  static private final Summary receivedBytes = Summary.build().name("requests_size_bytes")
      .help("Request size in bytes.").register();
  static protected final Summary requestLatency = Summary.build().quantile(0.5, 0.01)
      .quantile(0.95, 0.01).quantile(0.99, 0.01).quantile(1, 0.01).name("requests_latency_seconds")
      .help("Request latency in seconds.").register();

  @Override
  public void process(HttpResponse response, HttpContext context)
      throws HttpException, IOException {
    Timer timer = (Timer) context.getAttribute(TIMER);
    double duration = timer.observeDuration();

    // HttpRequestWrapper request = (HttpRequestWrapper) context
    // .getAttribute(HttpCacheContext.HTTP_REQUEST);
    // LOGGER.info("latency: {}µs\t{}", Math.round(duration * 1000000.0), request.getURI());

    receivedBytes
        .observe(response.getEntity() == null ? 0 : response.getEntity().getContentLength());
  }

  public double getBytes() {
    return receivedBytes.collect().get(0).samples.stream()
        .filter(sample -> "requests_size_bytes_sum".equals(sample.name)).findAny().get().value;
  }

  public double getTotalLatency() {
    return requestLatency.collect().get(0).samples.stream()
        .filter(sample -> "requests_latency_seconds_sum".equals(sample.name)).findAny().get().value;
  }

  public void logLatencies() {
    List<MetricFamilySamples> results = requestLatency.collect();
    Stream<Sample> quantiles = results.get(0).samples.stream()
        .filter(sample -> sample.labelNames.contains("quantile"));
    quantiles.forEachOrdered(sample -> {
      LOGGER.info("latency - {}th percentile: {}µs",
          (int) (Double.parseDouble(sample.labelValues.get(0)) * 100),
          Math.round(sample.value * 1000000));
    });

  }

  public void clear() {
    receivedBytes.clear();
    requestLatency.clear();
  }

  public double getLatency(int percentile) {
    List<MetricFamilySamples> results = requestLatency.collect();
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

  @Override
  public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
    context.setAttribute(TIMER, requestLatency.startTimer());
  }
}