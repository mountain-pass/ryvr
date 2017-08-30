package au.com.mountainpass.ryvr.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.sql.DataSource;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.mountainpass.TrustStoreManager;
import au.com.mountainpass.WebDriverFactory;
import au.com.mountainpass.ryvr.testclient.HtmlRyvrClient;
import au.com.mountainpass.ryvr.testclient.JavaRyvrClient;
import au.com.mountainpass.ryvr.testclient.RestRyvrClient;
import au.com.mountainpass.ryvr.testclient.RyvrTestClient;

@Configuration
public class TestConfiguration
    implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

  @Value("${server.ssl.protocol:TLS}")
  private String sslProtocol;

  @Value("${javax.net.ssl.trustStorePassword:changeit}")
  private String trustStorePassword;

  @Value("${javax.net.ssl.trustStoreType:JKS}")
  private String trustStoreType;

  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private ObjectMapper objectMapper;

  private int port;

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

  @Autowired(required = false)
  private WebDriverFactory webDriverFactory;

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public CloseableHttpAsyncClient asyncHttpClient() throws Exception {
    return asyncHttpClientBuilder().build();
  }

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public HttpAsyncClientBuilder asyncHttpClientBuilder() throws Exception {
    final NHttpClientConnectionManager connectionManager = nHttpClientConntectionManager();
    final RequestConfig config = httpClientRequestConfig();
    return HttpAsyncClientBuilder.create().setConnectionManager(connectionManager)
        .setConnectionManagerShared(true).setDefaultRequestConfig(config)
        .setSSLContext(sslContext());
  }

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public AsyncClientHttpRequestFactory asyncHttpClientFactory() throws Exception {
    final HttpComponentsAsyncClientHttpRequestFactory factory = new HttpComponentsAsyncClientHttpRequestFactory(
        httpClient(), asyncHttpClient());
    factory.setReadTimeout(200000);
    return factory;
  }

  // public URI getBaseUri() {
  // return URI.create("https://" + sslHostname + ":" + getPort());
  // }

  // public int getPort() {
  // return port;
  // }

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public CloseableHttpClient httpClient() throws Exception {

    CloseableHttpClient client = httpClientBuilder().build();
    return client;
  }

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public CachingHttpClientBuilder httpClientBuilder() throws Exception {
    final HttpClientConnectionManager connectionManager = httpClientConnectionManager();
    final RequestConfig config = httpClientRequestConfig();
    CacheConfig cacheConfig = cacheConfig();

    CachingHttpClientBuilder clientBuilder = (CachingHttpClientBuilder) CachingHttpClients.custom()
        .setCacheConfig(cacheConfig).setConnectionManager(connectionManager)
        .setDefaultRequestConfig(config).setSSLSocketFactory(sslSocketFactory())
        .setSSLContext(sslContext()).setRedirectStrategy(RyvrRedirectStrategy.INSTANCE)
        .addInterceptorLast((HttpRequestInterceptor) httpThroughputCounter)
        .addInterceptorLast((HttpRequestInterceptor) httpDelayConcurrent)
        .addInterceptorLast((HttpResponseInterceptor) httpDelayConcurrent)
        .addInterceptorLast((HttpResponseInterceptor) httpThroughputCounter)
        .addInterceptorLast(httpCacheStatusHeaderAdder);
    return clientBuilder;
  }

  @Autowired(required = false)
  private HttpCacheStatusHeaderAdder httpCacheStatusHeaderAdder;

  @Autowired(required = false)
  private HttpThroughputCounter httpThroughputCounter;

  @Autowired(required = false)
  private HttpDelayConcurrent httpDelayConcurrent;

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public CacheConfig cacheConfig() {
    return CacheConfig.custom().setMaxCacheEntries(10000000).setMaxObjectSize(8192 * 1024).build();
  }

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public HttpClientConnectionManager httpClientConnectionManager() throws Exception {
    final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
        httpConnectionSocketFactoryRegistry());
    connectionManager.setMaxTotal(proxyMaxConnectionsTotal);
    connectionManager.setDefaultMaxPerRoute(proxyMaxConnectionsRoute);
    return connectionManager;
  }

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public HttpComponentsClientHttpRequestFactory httpClientFactory() throws Exception {
    final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
        httpClient());
    factory.setReadTimeout(200000);
    return factory;
  }

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public Registry<ConnectionSocketFactory> httpConnectionSocketFactoryRegistry() throws Exception {
    return RegistryBuilder.<ConnectionSocketFactory> create()
        .register("http", PlainConnectionSocketFactory.getSocketFactory())
        .register("https", sslSocketFactory()).build();
  }

  @Bean
  @Profile(value = { "javaApi" })
  public RyvrTestClient javaClient() {
    return new JavaRyvrClient();
  }

  @Bean
  public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
    final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(
        objectMapper);
    return converter;
  }

  @Override
  public void onApplicationEvent(final EmbeddedServletContainerInitializedEvent event) {
    this.port = event.getEmbeddedServletContainer().getPort();
  }

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public RyvrTestClient restClient() {
    return new RestRyvrClient();
  }

  @Bean
  @Profile(value = { "restApi", "systemTest" })
  public RestTemplate restTemplate() throws Exception {
    RestTemplate restTemplate = new RestTemplate(httpClientFactory());
    return restTemplate;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  @Bean
  @Profile(value = { "ui" })
  public RyvrTestClient uiClient() {
    return new HtmlRyvrClient();
  }

  @Bean
  @Profile("ui")
  public WebDriver webDriver() throws ClassNotFoundException, NoSuchMethodException,
      SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, IOException {
    final WebDriver webDriver = webDriverFactory.createWebDriver();
    return webDriver;
  }

  @Bean // (destroyMethod = "close")
  public CloseableHttpAsyncClient httpAsyncClient() throws Exception {
    final CloseableHttpAsyncClient client = httpAsyncClientBuilder().build();
    client.start();
    return client;
  }

  @Bean
  public HttpAsyncClientBuilder httpAsyncClientBuilder() throws Exception {
    return HttpAsyncClientBuilder.create().setSSLContext(sslContext())
        .setConnectionManager(nHttpClientConntectionManager())
        .setDefaultRequestConfig(httpClientRequestConfig());
  }

  @Bean
  public RequestConfig httpClientRequestConfig() {
    final RequestConfig config = RequestConfig.custom().setConnectTimeout(proxyReadTimeoutMs)
        .build();
    return config;
  }

  @Autowired
  private Certificate cert;

  @Value("${server.ssl.key-alias}")
  private String keyAlias;

  @Bean
  public TrustStoreManager trustStoreManager()
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    TrustStoreManager trustStoreManager = new TrustStoreManager(trustStoreFile, trustStoreType,
        trustStorePassword);
    if (trustStoreManager.isSystemDefaultTrustStore()) {
      LOGGER.warn(
          "Trust Store location {} appears to be set to system default. The Self signed cert for testing will not be added and the tests will likely fail.",
          trustStoreManager.getTrustStoreLocation());
    } else {
      trustStoreManager.addCert(keyAlias, cert);
    }
    return trustStoreManager;
  }

  @Bean
  public NHttpClientConnectionManager nHttpClientConntectionManager() throws Exception {
    final PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(
        new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT), schemeIOSessionStrategyRegistry());
    connectionManager.setMaxTotal(proxyMaxConnectionsTotal);
    connectionManager.setDefaultMaxPerRoute(proxyMaxConnectionsRoute);
    return connectionManager;
  }

  @Bean
  Registry<SchemeIOSessionStrategy> schemeIOSessionStrategyRegistry() throws Exception {
    return RegistryBuilder.<SchemeIOSessionStrategy> create()
        .register("http", NoopIOSessionStrategy.INSTANCE)
        .register("https", new SSLIOSessionStrategy(sslContext())).build();
  }

  @Bean
  public SSLContext sslContext() throws Exception {
    final SSLContext sslContext = SSLContext.getInstance(sslProtocol);
    final TrustManagerFactory tmf = trustManagerFactory();
    final KeyStore ks = trustStoreManager().getKeyStore();
    tmf.init(ks);
    sslContext.init(null, tmf.getTrustManagers(), null);
    return sslContext;
  }

  @Bean
  public SSLConnectionSocketFactory sslSocketFactory() throws Exception {
    final SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(sslContext());
    return sf;
  }

  @Bean
  public TrustManagerFactory trustManagerFactory() throws NoSuchAlgorithmException {
    final TrustManagerFactory tmf = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
    return tmf;
  }

  @Autowired
  private DataSource dataSource;

  class MyDelegatingDS extends DelegatingDataSource {
    private final String catalogName;

    public MyDelegatingDS(final String catalogName, final DataSource dataSource) {
      super(dataSource);
      this.catalogName = catalogName;
    }

    @Override
    public Connection getConnection() throws SQLException {
      final Connection cnx = super.getConnection();
      cnx.setCatalog(this.catalogName);
      return cnx;
    }

  }

}
