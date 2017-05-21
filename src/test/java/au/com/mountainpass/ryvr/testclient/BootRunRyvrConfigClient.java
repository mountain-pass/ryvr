package au.com.mountainpass.ryvr.testclient;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.config.SslConfig;
import au.com.mountainpass.ryvr.config.TestConfiguration;
import au.com.mountainpass.ryvr.testclient.model.Health;
import cucumber.api.Scenario;

@Component
@Profile(value = { "systemTest" })
public class BootRunRyvrConfigClient implements RyvrTestConfigClient {

    private static final String RUN_DIR = "build/bootrun";
    private static final String APPLICATION_YML = RUN_DIR + "/application.yml";

    @Autowired
    private JdbcTemplate currentJt;

    private final List<Map<String, String>> dataSourcesRyvrConfigs = new ArrayList<>();

    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RyvrConfiguration ryvrConfig;

    private Process server;

    @Value("${spring.datasource.password}")
    private String springDatasourcePassword;

    @Value("${spring.datasource.url}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username}")
    private String springDatasourceUsername;

    @Autowired
    private SslConfig sslConfig;

    @Autowired
    private TestConfiguration testConfig;

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

    private void createApplicationProperties() throws IOException {
        new File(RUN_DIR).mkdirs();
        new File(APPLICATION_YML).delete();
        final FileWriter fileWriter = new FileWriter(APPLICATION_YML);
        final StringWriter writer = new StringWriter();
        writer.write("server:\n");
        writer.write("  ssl:\n");
        writer.write("    key-store: build/bootrun/keystore.jks\n");
        writer.write("au.com.mountainpass.ryvr:\n");
        writer.write("  data-sources:\n");
        writer.write("    - url: " + springDatasourceUrl + "\n");
        writer.write("      username: " + springDatasourceUsername + "\n");
        writer.write("      password: " + springDatasourcePassword + "\n");
        if (!dataSourcesRyvrConfigs.isEmpty()) {
            writer.write("      ryvrs:\n");
            for (final Map<String, String> config : dataSourcesRyvrConfigs) {
                writer.write("        " + config.get("name") + ":\n");
                writer.write("          page-size: " + config.get("page size")
                        + "\n");
                writer.write(
                        "          catalog: " + config.get("database") + "\n");
                writer.write("          table: " + config.get("table") + "\n");
                writer.write("          ordered-by: " + config.get("ordered by")
                        + "\n");
            }
        }
        writer.close();
        LOGGER.info("CONFIG:\r\n{}", writer.getBuffer().toString());
        fileWriter.write(writer.getBuffer().toString());
        fileWriter.close();
    }

    @Override
    public void createDatabase(final String dbName) throws Throwable {
        final Connection connection = currentJt.getDataSource().getConnection();
        final String identifierQuoteString = connection.getMetaData()
                .getIdentifierQuoteString();

        final String dbProductName = connection.getMetaData()
                .getDatabaseProductName();
        LOGGER.info("dbProductName: {}", dbProductName);
        switch (dbProductName) {
        case "H2":
            currentJt.execute(
                    "CREATE SCHEMA IF NOT EXISTS " + identifierQuoteString
                            + dbName + identifierQuoteString + ";");
            break;
        case "MySQL":
            currentJt.execute(
                    "CREATE DATABASE IF NOT EXISTS " + identifierQuoteString
                            + dbName + identifierQuoteString + ";");
            break;
        }
        connection.setCatalog(dbName);
        connection.close();
    }

    @Override
    public void createDataSourceRyvr(final Map<String, String> config)
            throws Throwable {
        this.dataSourcesRyvrConfigs.add(config);
    }

    @Override
    public void createTable(final String catalog, final String table)
            throws Throwable {
        final Connection connection = currentJt.getDataSource().getConnection();
        final String identifierQuoteString = connection.getMetaData()
                .getIdentifierQuoteString();
        final String catalogSeparator = connection.getMetaData()
                .getCatalogSeparator();
        currentJt.execute("drop table if exists " + identifierQuoteString
                + catalog + identifierQuoteString + catalogSeparator
                + identifierQuoteString + table + identifierQuoteString);

        final StringBuilder statementBuffer = new StringBuilder();
        statementBuffer.append("create table ");
        statementBuffer.append(identifierQuoteString + catalog
                + identifierQuoteString + catalogSeparator);
        statementBuffer
                .append(identifierQuoteString + table + identifierQuoteString);
        statementBuffer.append(
                " (ID INT, ACCOUNT VARCHAR(255), DESCRIPTION VARCHAR(255), AMOUNT Decimal(19,4), CONSTRAINT PK_ID PRIMARY KEY (ID))");
        currentJt.execute(statementBuffer.toString());
        currentJt.update("DELETE FROM " + identifierQuoteString + catalog
                + identifierQuoteString + catalogSeparator
                + identifierQuoteString + table + identifierQuoteString);
        final DatabaseMetaData md = connection.getMetaData();
        final ResultSet rs = md.getTables(null, null, "%", null);
        while (rs.next()) {
            LOGGER.debug("TABLE: {}", rs.getString(3));
        }
        connection.close();
    }

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void ensureStarted() throws Throwable {
        createApplicationProperties();
        // ../../gradlew -p ../..
        final ProcessBuilder pb = new ProcessBuilder("sh", "./gradlew",
                "-Dspring.config.location=" + APPLICATION_YML, "bootRun")
                        .inheritIO();
        server = pb.start();
        testConfig.setPort(8443);
        ryvrConfig.setPort(8443);

        final URI baseUri = ryvrConfig.getBaseUri();
        final String host = baseUri.getHost();
        final int port = baseUri.getPort();

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

    private boolean hostAvailable(final String host, final int port)
            throws UnknownHostException {
        try (Socket s = new Socket(host, port)) {
            s.close();
            return true;
        } catch (final IOException e) {
            LOGGER.info("Status: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void insertRows(final String catalog, final String table,
            final List<Map<String, String>> events) throws Throwable {
        final Connection connection = currentJt.getDataSource().getConnection();
        final String identifierQuoteString = connection.getMetaData()
                .getIdentifierQuoteString();
        final String catalogSeparator = connection.getMetaData()
                .getCatalogSeparator();

        currentJt.batchUpdate(
                "insert into " + identifierQuoteString + catalog
                        + identifierQuoteString + catalogSeparator
                        + identifierQuoteString + table + identifierQuoteString
                        + "(ID, ACCOUNT, DESCRIPTION, AMOUNT) values (?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public int getBatchSize() {
                        return events.size();
                    }

                    @Override
                    public void setValues(final PreparedStatement ps,
                            final int i) throws SQLException {
                        // TODO Auto-generated method stub
                        final Map<String, String> row = events.get(i);
                        ps.setInt(1, Integer.parseInt(row.get("ID")));
                        ps.setString(2, row.get("ACCOUNT"));
                        ps.setString(3, row.get("DESCRIPTION"));
                        ps.setBigDecimal(4, new BigDecimal(row.get("AMOUNT")));
                    }

                });
        connection.close();
    }

    @PostConstruct
    private void postConstruct() {
        final BootRunRyvrConfigClient config = this;
        final Thread closeChildThread = new Thread() {
            @Override
            public void run() {
                config.stop();
            }
        };

        Runtime.getRuntime().addShutdownHook(closeChildThread);
    }

    public void stop() {
        if (server != null) {
            server.destroy();
            while (server.isAlive()) {
                LOGGER.info("Waiting for server to terminate...");
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            final URI baseUri = ryvrConfig.getBaseUri();
            final String host = baseUri.getHost();
            final int port = baseUri.getPort();

            LOGGER.info("Waiting for server to stop listening: {}:{}", host,
                    port);
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
    }

}
