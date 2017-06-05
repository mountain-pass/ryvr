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
package au.com.mountainpass.ryvr;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

@EnableAsync
/*
 * _ @Import({ AuditAutoConfiguration.class, DataSourceAutoConfiguration.class,
 * DataSourcePoolMetadataProvidersConfiguration.class,
 * DataSourceTransactionManagerAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
 * EmbeddedServletContainerAutoConfiguration.class, EndpointAutoConfiguration.class,
 * EndpointMBeanExportAutoConfiguration.class, EndpointWebMvcAutoConfiguration.class,
 * EndpointWebMvcManagementContextConfiguration.class, ErrorMvcAutoConfiguration.class,
 * HttpEncodingAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class, //
 * InfoContributorAutoConfiguration.class, JacksonAutoConfiguration.class,
 * JdbcTemplateAutoConfiguration.class, // JmxAutoConfiguration.class,
 * ManagementServerPropertiesAutoConfiguration.class, // MetricExportAutoConfiguration.class, //
 * MetricFilterAutoConfiguration.class, // MetricRepositoryAutoConfiguration.class,
 * MultipartAutoConfiguration.class, PersistenceExceptionTranslationAutoConfiguration.class, //
 * ProjectInfoAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class, //
 * PublicMetricsAutoConfiguration.class, ServerPropertiesAutoConfiguration.class, //
 * TraceRepositoryAutoConfiguration.class, // TraceWebFilterAutoConfiguration.class,
 * TransactionAutoConfiguration.class, // ValidationAutoConfiguration.class,
 * WebClientAutoConfiguration.class, WebMvcAutoConfiguration.class })
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(value = "au.com.mountainpass")
public class Application {
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /**
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
    final FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
    bean.setOrder(0);
    return bean;
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

}
