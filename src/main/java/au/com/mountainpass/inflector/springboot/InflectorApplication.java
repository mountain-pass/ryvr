/*
 *  Copyright 2016 SmartBear Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package au.com.mountainpass.inflector.springboot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.AuditAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointMBeanExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpEncodingAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@EnableAsync
@Import({ AuditAutoConfiguration.class, DataSourceAutoConfiguration.class,
        DataSourcePoolMetadataProvidersConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        DispatcherServletAutoConfiguration.class,
        EmbeddedServletContainerAutoConfiguration.class,
        EndpointAutoConfiguration.class,
        EndpointMBeanExportAutoConfiguration.class,
        EndpointWebMvcAutoConfiguration.class,
        EndpointWebMvcManagementContextConfiguration.class,
        ErrorMvcAutoConfiguration.class, HttpEncodingAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        // InfoContributorAutoConfiguration.class,
        JacksonAutoConfiguration.class, JdbcTemplateAutoConfiguration.class, // JmxAutoConfiguration.class,
        ManagementServerPropertiesAutoConfiguration.class,
        // MetricExportAutoConfiguration.class,
        // MetricFilterAutoConfiguration.class,
        // MetricRepositoryAutoConfiguration.class,
        MultipartAutoConfiguration.class,
        PersistenceExceptionTranslationAutoConfiguration.class,
        // ProjectInfoAutoConfiguration.class,
        PropertyPlaceholderAutoConfiguration.class,
        // PublicMetricsAutoConfiguration.class,
        ServerPropertiesAutoConfiguration.class,
        // TraceRepositoryAutoConfiguration.class,
        // TraceWebFilterAutoConfiguration.class,
        TransactionAutoConfiguration.class, // ValidationAutoConfiguration.class,
        WebClientAutoConfiguration.class, WebMvcAutoConfiguration.class })
@Configuration
@ComponentScan(value = "au.com.mountainpass")
public class InflectorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(InflectorApplication.class, args);
    }

    @Value("${au.com.windyroad.service-gateway.proxy.max.connections.route:20}")
    private int proxyMaxConnectionsRoute;

    @Value("${au.com.windyroad.service-gateway.proxy.max.connections.total:100}")
    private int proxyMaxConnectionsTotal;

    @Value("${au.com.windyroad.service-gateway.proxy.read.timeout.ms:60000}")
    private int proxyReadTimeoutMs;

    @Value("${server.ssl.protocol:TLS}")
    private String sslProtocol;

    @Value("${javax.net.ssl.trustStore:}")
    private String trustStore;

    @Value("${javax.net.ssl.trustStorePassword:changeit}")
    private String trustStorePassword;

    @Value("${javax.net.ssl.trustStoreType:JKS}")
    private String trustStoreType;

    // @Bean
    // public Configuration configuration(
    // final ApplicationContext applicationContext) {
    // final Configuration configuration = Configuration.read();
    // configuration.setControllerFactory(
    // (cls, operation) -> applicationContext.getBean(cls));
    // return configuration;
    // }

    /**
     * Since we're using both Actuator and Jersey, we need to use Springs
     * <a href=
     * "http://docs.spring.io/spring/docs/current/spring-framework-reference/html/cors.html#_filter_based_cors_support">
     * Filter based CORS support</a>
     *
     * @return corsFilter
     */
    @Bean
    public FilterRegistrationBean corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        final FilterRegistrationBean bean = new FilterRegistrationBean(
                new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

    public String getTrustStoreLocation() {
        if (StringUtils.hasLength(trustStore)) {
            return trustStore;
        }
        final String locationProperty = System
                .getProperty("javax.net.ssl.trustStore");
        if (StringUtils.hasLength(locationProperty)) {
            return locationProperty;
        } else {
            return systemDefaultTrustStoreLocation();
        }
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    @Bean // (destroyMethod = "close")
    public CloseableHttpAsyncClient httpAsyncClient() throws Exception {
        final CloseableHttpAsyncClient client = httpAsyncClientBuilder()
                .build();
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
    RequestConfig httpClientRequestConfig() {
        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(proxyReadTimeoutMs).build();
        return config;
    }

    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:locale/messages");
        messageSource.setCacheSeconds(3600); // refresh cache once per hour
        return messageSource;
    }

    @Bean
    public NHttpClientConnectionManager nHttpClientConntectionManager()
            throws Exception {
        final PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(
                new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT),
                schemeIOSessionStrategyRegistry());
        connectionManager.setMaxTotal(proxyMaxConnectionsTotal);
        connectionManager.setDefaultMaxPerRoute(proxyMaxConnectionsRoute);
        return connectionManager;
    }

    @Bean
    Registry<SchemeIOSessionStrategy> schemeIOSessionStrategyRegistry()
            throws Exception {
        return RegistryBuilder.<SchemeIOSessionStrategy> create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", new SSLIOSessionStrategy(sslContext()))
                .build();
    }

    @Bean
    public SSLContext sslContext() throws Exception {
        final SSLContext sslContext = SSLContext.getInstance(sslProtocol);
        final TrustManagerFactory tmf = trustManagerFactory();
        final KeyStore ks = trustStore();
        tmf.init(ks);
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }

    @Bean
    public SSLConnectionSocketFactory sslSocketFactory() throws Exception {
        final SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(
                sslContext());
        return sf;
    }

    public String systemDefaultTrustStoreLocation() {
        final String javaHome = System.getProperty("java.home");
        final FileSystemResource location = new FileSystemResource(
                javaHome + "/lib/security/jssecacerts");
        if (location.exists()) {
            return location.getFilename();
        } else {
            return javaHome + "/lib/security/cacerts";
        }
    }

    @Bean
    TrustManagerFactory trustManagerFactory() throws NoSuchAlgorithmException {
        final TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        return tmf;
    }

    @Bean
    KeyStore trustStore()
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, FileNotFoundException {
        final KeyStore ks = KeyStore.getInstance(trustStoreType);

        final File trustFile = new File(getTrustStoreLocation());
        ks.load(new FileInputStream(trustFile),
                trustStorePassword.toCharArray());
        return ks;
    }

}
