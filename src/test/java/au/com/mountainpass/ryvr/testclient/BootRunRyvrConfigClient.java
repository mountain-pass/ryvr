package au.com.mountainpass.ryvr.testclient;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.TrustStoreManager;
import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.config.SslConfig;
import au.com.mountainpass.ryvr.config.TestConfiguration;
import au.com.mountainpass.ryvr.testclient.model.Health;
import cucumber.api.Scenario;

@Component
@Profile(value = { "systemTest" })
public class BootRunRyvrConfigClient implements RyvrTestConfigClient {
    private static final String PROJECT_DIR = "../..";

    private static final String RUN_DIR = "build/bootrun";

    private static final String APPLICATION_YML = RUN_DIR + "/application.yml";

    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JdbcTemplate currentJt;

    private List<Map<String, String>> dataSourcesRyvrConfigs = new ArrayList<>();

    private Process server;

    @Value("${server.ssl.protocol:TLS}")
    private String sslProtocol;

    @Value("${au.com.mountainpass.ryvr.proxy.max.connections.route:20}")
    private int proxyMaxConnectionsRoute;

    @Value("${au.com.mountainpass.ryvr.proxy.max.connections.total:100}")
    private int proxyMaxConnectionsTotal;

    @Value("${au.com.mountainpass.ryvr.proxy.read.timeout.ms:60000}")
    private int proxyReadTimeoutMs;

    @Value("${au.com.mountainpass.ryvr.ssl.hostname}")
    private String sslHostname;

    @Value("${javax.net.ssl.trustStore:build/truststore.jks}")
    private String trustStoreFile;

    @Value("${javax.net.ssl.trustStorePassword:changeit}")
    private String trustStorePassword;

    @Value("${javax.net.ssl.trustStoreType:JKS}")
    private String trustStoreType;

    @Value("${spring.datasource.url}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username}")
    private String springDatasourceUsername;

    @Value("${spring.datasource.password}")
    private String springDatasourcePassword;

    @Override
    public void createDatabase(String dbName) throws Throwable {
        Connection connection = currentJt.getDataSource().getConnection();
        String identifierQuoteString = connection.getMetaData()
                .getIdentifierQuoteString();

        String dbProductName = connection.getMetaData()
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
    public void createDataSourceRyvr(Map<String, String> config)
            throws Throwable {
        this.dataSourcesRyvrConfigs.add(config);
    }

    @Override
    public void insertRows(String catalog, String table,
            List<Map<String, String>> events) throws Throwable {
        Connection connection = currentJt.getDataSource().getConnection();
        String identifierQuoteString = connection.getMetaData()
                .getIdentifierQuoteString();
        String catalogSeparator = connection.getMetaData()
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

    @Override
    public void createTable(String catalog, String table) throws Throwable {
        Connection connection = currentJt.getDataSource().getConnection();
        String identifierQuoteString = connection.getMetaData()
                .getIdentifierQuoteString();
        String catalogSeparator = connection.getMetaData()
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
        DatabaseMetaData md = connection.getMetaData();
        ResultSet rs = md.getTables(null, null, "%", null);
        while (rs.next()) {
            LOGGER.debug("TABLE: {}", rs.getString(3));
        }
        connection.close();
    }

    @Override
    public void _before(Scenario scenario) {
        clearRyvrs();
    }

    @Override
    public void clearRyvrs() {
        stop();
    }

    @Autowired
    TestConfiguration testConfig;

    @Autowired
    RyvrConfiguration ryvrConfig;

    @PostConstruct
    private void postConstruct() {
        BootRunRyvrConfigClient config = this;
        Thread closeChildThread = new Thread() {
            @Override
            public void run() {
                config.stop();
            }
        };

        Runtime.getRuntime().addShutdownHook(closeChildThread);
    }

    @Autowired
    private SslConfig sslConfig;

    @Autowired
    private TrustStoreManager trustStoreManager;

    @Value("${server.ssl.key-alias}")
    private String keyAlias;

    @Autowired
    private SSLContext sslContext;

    @Autowired
    private TrustManagerFactory trustManagerFactory;

    private Certificate cert;

    public static boolean hostAvailable(String host, int port)
            throws UnknownHostException, IOException {
        try (Socket s = new Socket(host, port)) {
            s.close();
            return true;
        }
    }

    @Override
    public void ensureStarted() throws Throwable {
        createApplicationProperties();
        // ../../gradlew -p ../..
        ProcessBuilder pb = new ProcessBuilder("sh", PROJECT_DIR + "/gradlew",
                "-p", PROJECT_DIR, "bootRun").inheritIO()
                        .directory(new File(RUN_DIR));
        server = pb.start();
        testConfig.setPort(8443);
        ryvrConfig.setPort(8443);

        URI baseUri = ryvrConfig.getBaseUri();
        String host = baseUri.getHost();
        int port = baseUri.getPort();

        LOGGER.info("Waiting for server to start: {}:{}", host, port);
        for (int i = 0; i < 30 && server.isAlive(); ++i) {
            try {
                if (hostAvailable(host, port)) {
                    break;
                }
            } catch (ResourceAccessException | ConnectException e) {
                LOGGER.info("Status: {}", e.getMessage());
            }
            Thread.sleep(1000);
        }
        assertTrue(server.isAlive());
        LOGGER.info("Connected to server: {}:{}", host, port);

        cert = sslConfig.cert();

        RestTemplate restTemplate = new RestTemplate(httpClientFactory());

        LOGGER.info("Waiting for UP status: {}:{}", host, port);

        for (int i = 0; i < 30 && server.isAlive(); ++i) {
            try {
                ResponseEntity<Health> health = restTemplate
                        .getForEntity(baseUri.resolve("/health"), Health.class);
                LOGGER.info("Status Response: {}", health);
                LOGGER.info("Status: {}", health.getBody().status);
                if (Status.UP.equals(health.getBody().status)) {
                    return;
                }
            } catch (Exception e) {
                LOGGER.info("Status: {}", e.getMessage());
            }
            Thread.sleep(1000);
        }
        assertTrue(server.isAlive());

        throw new TimeoutException("timeout waiting for server to start");
    }

    private void createApplicationProperties() throws IOException {
        new File(RUN_DIR).mkdirs();
        new File(APPLICATION_YML).delete();
        FileWriter writer = new FileWriter(APPLICATION_YML);
        writer.write("server:\n");
        // writer.write(" port: 8443\n");
        // writer.write(" ssl:\n");
        writer.write(" key-store: build/bootrun/keystore.jks\n");
        // writer.write(" key-store-password: secret\n");
        // writer.write(" key-password: secret\n");
        // writer.write(" key-alias: selfSigned\n");
        writer.write("au.com.mountainpass.ryvr:\n");
        // writer.write(" ssl:\n");
        // writer.write(" hostname: localhost\n");
        // writer.write(" genCert: selfSigned\n");
        writer.write("  data-sources:\n");
        writer.write("    - url: " + springDatasourceUrl + "\n");
        writer.write("      username: " + springDatasourceUsername + "\n");
        writer.write("      password: " + springDatasourcePassword + "\n");
        writer.write("      ryvrs:\n");
        for (Map<String, String> config : dataSourcesRyvrConfigs) {
            writer.write("        " + config.get("name") + ":\n");
            writer.write(
                    "          page-size: " + config.get("page size") + "\n");
            writer.write("          catalog: " + config.get("database") + "\n");
            writer.write("          table: " + config.get("table") + "\n");
            writer.write(
                    "          ordered-by: " + config.get("ordered by") + "\n");
        }
        writer.close();
    }

    public HttpComponentsClientHttpRequestFactory httpClientFactory()
            throws Exception {
        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
                httpClient());
        factory.setReadTimeout(200000);
        return factory;
    }

    public CloseableHttpClient httpClient() throws Exception {
        return httpClientBuilder().build();
    }

    public HttpClientBuilder httpClientBuilder() throws Exception {
        final HttpClientConnectionManager connectionManager = httpClientConnectionManager();
        final RequestConfig config = httpClientRequestConfig();
        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(config)
                .setSSLSocketFactory(sslSocketFactory())
                .setSslcontext(sslContext()).disableRedirectHandling();
    }

    public HttpClientConnectionManager httpClientConnectionManager()
            throws Exception {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                httpConnectionSocketFactoryRegistry());
        connectionManager.setMaxTotal(proxyMaxConnectionsTotal);
        connectionManager.setDefaultMaxPerRoute(proxyMaxConnectionsRoute);
        return connectionManager;
    }

    public RequestConfig httpClientRequestConfig() {
        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(proxyReadTimeoutMs).build();
        return config;
    }

    public SSLConnectionSocketFactory sslSocketFactory() throws Exception {
        final SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(
                sslContext());
        return sf;
    }

    public SSLContext sslContext() throws Exception {
        final SSLContext sslContext = SSLContext.getInstance(sslProtocol);
        final TrustManagerFactory tmf = trustManagerFactory();
        final KeyStore ks = trustStoreManager().getKeyStore();
        tmf.init(ks);
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }

    public Registry<ConnectionSocketFactory> httpConnectionSocketFactoryRegistry()
            throws Exception {
        return RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http",
                        PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory()).build();
    }

    TrustManagerFactory trustManagerFactory() throws NoSuchAlgorithmException {
        final TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        return tmf;
    }

    public TrustStoreManager trustStoreManager() throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {
        TrustStoreManager trustStoreManager = new TrustStoreManager(
                trustStoreFile, trustStoreType, trustStorePassword);
        if (trustStoreManager.isSystemDefaultTrustStore()) {
            LOGGER.warn(
                    "Trust Store location {} appears to be set to system default. The Self signed cert for testing will not be added and the tests will likely fail.",
                    trustStoreManager.getTrustStoreLocation());
        } else {
            LOGGER.info("Adding certificate to trust store: {}\n{}", keyAlias,
                    cert);
            trustStoreManager.addCert(keyAlias, cert);
        }
        return trustStoreManager;
    }

    @Override
    public void _after(Scenario scenario) {
        stop();
    }

    public void stop() {
        if (server != null) {
            server.destroy();
            server = null;
        }
    }

}
