package au.com.mountainpass.ryvr.testclient;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.config.TestConfiguration;
import au.com.mountainpass.ryvr.testclient.model.Health;
import cucumber.api.Scenario;

@Component
@Profile(value = { "systemTest" })
public class RyvrTestExternalServerAdminDriver implements RyvrTestServerAdminDriver {

  private class Shutdowner extends Thread {
    private RyvrTestExternalServerAdminDriver driver;

    Shutdowner(RyvrTestExternalServerAdminDriver driver) {
      this.driver = driver;
      Runtime.getRuntime().addShutdownHook(this);
    }

    @Override
    public void run() {
      driver.stop();
    }
  }

  final List<Map<String, String>> dataSourcesRyvrConfigs = new ArrayList<>();

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private RyvrConfiguration ryvrConfig;

  private Process server;

  @Value("${spring.datasource.password}")
  String springDatasourcePassword;

  @Value("${spring.datasource.url}")
  String springDatasourceUrl;

  @Value("${spring.datasource.username}")
  String springDatasourceUsername;

  @Autowired
  private TestConfiguration testConfig;

  @Autowired
  RyvrTestServerProcessBuilder processBuilder;

  private Shutdowner shutdowner;

  @Override
  public void _after(final Scenario scenario) {
    stop();
  }

  @Override
  public void _before(final Scenario scenario) {
    clearRyvrs();
  }

  @Override
  public void clearRyvrs() {
    stop();
    dataSourcesRyvrConfigs.clear();
  }

  @Override
  public void createDataSourceRyvr(final Map<String, String> config) throws Throwable {
    this.dataSourcesRyvrConfigs.add(config);
  }

  @Override
  public void ensureStarted() throws Throwable {
    final URI baseUri = ryvrConfig.getBaseUri();
    final String host = baseUri.getHost();
    final int port = 8443;
    testConfig.setPort(port);
    ryvrConfig.setPort(port);

    if ((server == null || !server.isAlive()) && !hostAvailable(host, port)) {
      processBuilder.createApplicationProperties(dataSourcesRyvrConfigs);
      this.shutdowner = new Shutdowner(this);
      final ProcessBuilder pb = getProcessBuilder().inheritIO();
      server = pb.start();

      LOGGER.info("Waiting for server to start: {}:{}", host, port);
      for (int i = 0; i < 30 && server.isAlive(); ++i) {
        try {
          if (hostAvailable(host, port)) {
            break;
          }
        } catch (final UnknownHostException e) {
          throw new RuntimeException(e);
        }
        Thread.sleep(1000);
      }
      assertTrue(server.isAlive());
      LOGGER.info("Connected to server: {}:{}", host, port);

      LOGGER.info("Waiting for UP status: {}:{}", host, port);

      for (int i = 0; i < 30 && server.isAlive(); ++i) {
        try {
          final ResponseEntity<Health> health = restTemplate
              .getForEntity(baseUri.resolve("/health"), Health.class);
          LOGGER.info("Status Response: {}", health);
          LOGGER.info("Status: {}", health.getBody().status);
          if (Status.UP.equals(health.getBody().status)) {
            return;
          }
        } catch (final Exception e) {
          LOGGER.info("Status: {}", e.getMessage());
        }
        Thread.sleep(1000);
      }
      assertTrue(server.isAlive());

      throw new TimeoutException("timeout waiting for server to start");
    }
  }

  protected ProcessBuilder getProcessBuilder() throws IOException {
    return processBuilder.getProcessBuilder();
  }

  private boolean hostAvailable(final String host, final int port) throws UnknownHostException {
    try (Socket s = new Socket(host, port)) {
      s.close();
      return true;
    } catch (final IOException e) {
      LOGGER.info("Status: {}", e.getMessage());
      return false;
    }
  }

  @PostConstruct
  private void postConstruct() {
    final RyvrTestExternalServerAdminDriver config = this;
    final Thread closeChildThread = new Thread() {
      @Override
      public void run() {
        config.stop();
      }
    };

    Runtime.getRuntime().addShutdownHook(closeChildThread);
  }

  public void stop() {
    try {
      if (server != null) {
        server.destroy();
        shutdowner = null;
        while (server.isAlive()) {
          LOGGER.info("Waiting for server to terminate...");
          try {
            Thread.sleep(1000);
          } catch (final InterruptedException e) {
            // meh
          }
        }
        final URI baseUri = ryvrConfig.getBaseUri();
        final String host = baseUri.getHost();
        final int port = baseUri.getPort();

        LOGGER.info("Waiting for server to stop listening: {}:{}", host, port);
        boolean stopped = false;
        for (int i = 0; i < 30; ++i) {
          try {
            if (!hostAvailable(host, port)) {
              stopped = true;
              break;
            }
            LOGGER.info("waiting...");
            Thread.sleep(1000);
          } catch (InterruptedException | UnknownHostException e) {
            throw new RuntimeException(e);
          }
        }
        assertTrue(stopped);
        LOGGER.info("...Terminated");
        server = null;
      }
    } finally {
      if (shutdowner != null) {
        Runtime.getRuntime().removeShutdownHook(shutdowner);
      }
    }
  }

  @Override
  public void deleteRvyr(String name) throws Throwable {
    stop();
    dataSourcesRyvrConfigs.removeIf(config -> {
      return name.equals(config.get("name"));
    });
    ensureStarted();
  }

}
