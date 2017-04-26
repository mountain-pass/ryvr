package au.com.mountainpass.ryvr.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Set;

import org.apache.catalina.startup.Tomcat;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.glassfish.jersey.server.model.Resource;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.mountainpass.TestKeyStoreManager;
import au.com.mountainpass.WebDriverFactory;
import au.com.mountainpass.inflector.springboot.InflectorApplication;
import au.com.mountainpass.inflector.springboot.config.InflectorConfig;
import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;
import au.com.mountainpass.ryvr.testclient.JavaRyvrClient;
import au.com.mountainpass.ryvr.testclient.RestRyvrClient;
import au.com.mountainpass.ryvr.testclient.RyvrTestClient;

@Configuration
public class TestConfiguration implements
        ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    @Autowired
    private InflectorConfig config;

    @Autowired
    private InflectorApplication infelctorApplication;

    @Value("${server.ssl.key-alias}")
    private String keyAlias;

    @Value("${server.ssl.key-password}")
    private String keyPassword;

    @Value("${server.ssl.key-store}")
    private String keyStore;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;

    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ObjectMapper objectMapper;

    private int port;

    @Value("${au.com.windyroad.service-gateway.proxy.max.connections.route:20}")
    private int proxyMaxConnectionsRoute;

    @Value("${au.com.windyroad.service-gateway.proxy.max.connections.total:100}")
    private int proxyMaxConnectionsTotal;

    @Value("${au.com.windyroad.service-gateway.proxy.read.timeout.ms:60000}")
    private int proxyReadTimeoutMs;

    @Value("${au.com.windyroad.service-gateway.ssl.hostname}")
    private String sslHostname;

    @Value("${javax.net.ssl.trustStore:build/truststore.jks}")
    private String trustStoreFile;

    @Autowired(required = false)
    private WebDriverFactory webDriverFactory;

    @Bean
    public CloseableHttpAsyncClient asyncHttpClient() throws Exception {
        return asyncHttpClientBuilder().build();
    }

    @Bean
    public HttpAsyncClientBuilder asyncHttpClientBuilder() throws Exception {
        final NHttpClientConnectionManager connectionManager = infelctorApplication
                .nHttpClientConntectionManager();
        final RequestConfig config = httpClientRequestConfig();
        return HttpAsyncClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setConnectionManagerShared(true)
                .setDefaultRequestConfig(config)
                .setSSLContext(infelctorApplication.sslContext());
    }

    @Bean
    public AsyncClientHttpRequestFactory asyncHttpClientFactory()
            throws Exception {
        final HttpComponentsAsyncClientHttpRequestFactory factory = new HttpComponentsAsyncClientHttpRequestFactory(
                httpClient(), asyncHttpClient());
        factory.setReadTimeout(200000);
        return factory;
    }

    @Bean
    public EmbeddedDatabase db() {
        return new EmbeddedDatabaseBuilder().setName("TEST_DB")
                .setType(EmbeddedDatabaseType.H2).setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true).addScript("initH2.sql").build();

    }

    public URI getBaseUri() {
        return URI.create("https://" + sslHostname + ":" + getPort());
    }

    public int getPort() {
        return port;
    }

    @Bean
    public CloseableHttpClient httpClient() throws Exception {
        return httpClientBuilder().build();
    }

    @Bean
    public HttpClientBuilder httpClientBuilder() throws Exception {
        final HttpClientConnectionManager connectionManager = httpClientConnectionManager();
        final RequestConfig config = httpClientRequestConfig();
        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(config)
                .setSSLSocketFactory(infelctorApplication.sslSocketFactory())
                .setSslcontext(infelctorApplication.sslContext())
                .disableRedirectHandling();
    }

    @Bean
    public HttpClientConnectionManager httpClientConnectionManager()
            throws Exception {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                httpConnectionSocketFactoryRegistry());
        connectionManager.setMaxTotal(proxyMaxConnectionsTotal);
        connectionManager.setDefaultMaxPerRoute(proxyMaxConnectionsRoute);
        return connectionManager;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory httpClientFactory()
            throws Exception {
        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
                httpClient());
        factory.setReadTimeout(200000);
        return factory;
    }

    @Bean
    public RequestConfig httpClientRequestConfig() {
        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(proxyReadTimeoutMs).build();
        return config;
    }

    @Bean
    public Registry<ConnectionSocketFactory> httpConnectionSocketFactoryRegistry()
            throws Exception {
        return RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http",
                        PlainConnectionSocketFactory.getSocketFactory())
                .register("https", infelctorApplication.sslSocketFactory())
                .build();
    }

    @Bean
    @Profile(value = { "unitTest" })
    public RyvrTestClient javaClient() {
        return new JavaRyvrClient();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(db());
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(
                objectMapper);
        return converter;
    }

    @Override
    public void onApplicationEvent(
            final EmbeddedServletContainerInitializedEvent event) {
        this.port = event.getEmbeddedServletContainer().getPort();
    }

    @Bean
    @Profile(value = { "systemTest" })
    public RyvrTestClient restClient() {
        return new RestRyvrClient();
    }

    @Bean
    @Profile(value = { "systemTest" })
    public RestTemplate restTemplate() throws Exception {
        return new RestTemplate(httpClientFactory());
    }

    @Bean
    public TestKeyStoreManager serviceGatewayKeyStoreManager()
            throws Exception {
        if (infelctorApplication.getTrustStoreLocation().equals(
                infelctorApplication.systemDefaultTrustStoreLocation())) {
            LOGGER.warn(
                    "Trust Store location {} appears to be set to system default. The Self signed cert for testing will not be added and the tests will likely fail.",
                    infelctorApplication.getTrustStoreLocation());
            return new TestKeyStoreManager(keyStore, keyStorePassword,
                    keyPassword, keyAlias, sslHostname, null, null, null);
        }
        return new TestKeyStoreManager(keyStore, keyStorePassword, keyPassword,
                keyAlias, sslHostname,
                infelctorApplication.getTrustStoreLocation(),
                infelctorApplication.getTrustStorePassword(),
                infelctorApplication.getTrustStoreType());
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Bean
    public Object swaggerJsonHandler() {
        final Set<Resource> resources = config.getResources();
        for (final Resource r : resources) {
            if ("/swagger.json/".equals(r.getPath())) {
                final Set<Object> instances = r.getHandlerInstances();
                return instances;
            }
        }
        return null;
    }

    @Bean
    public TomcatEmbeddedServletContainerFactory tomcatFactory()
            throws Exception {
        serviceGatewayKeyStoreManager();
        return new TomcatEmbeddedServletContainerFactory() {

            @Override
            protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(
                    final Tomcat tomcat) {
                return super.getTomcatEmbeddedServletContainer(tomcat);
            }
        };
    }

    @Bean
    @Profile(value = { "uiTest" })
    public RyvrTestClient uiClient() {
        return new HtmlRyvrClient();
    }

    @Bean
    @Profile("uiTest")
    public WebDriver webDriver()
            throws ClassNotFoundException, NoSuchMethodException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, IOException {
        final WebDriver webDriver = webDriverFactory.createWebDriver();
        return webDriver;
    }

}
