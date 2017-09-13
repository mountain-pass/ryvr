package au.com.mountainpass.ryvr.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.reflect.ReflectionObjectHandler;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

  @Autowired
  private ObjectMapper om;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // registry.addResourceHandler("/api-docsxx/**").addResourceLocations("/webjars/swagger-ui/2.2.10/");
    // registry.addResourceHandler("/api-docsxx/swagger.json").addResourceLocations("/api/v2/swagger.json");
  }

  @Override
  public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
    configurer.defaultContentType(MediaType.TEXT_HTML);
    configurer.ignoreAcceptHeader(false).favorPathExtension(true).mediaType("json",
        MediaType.APPLICATION_JSON);
    configurer.mediaType("html", MediaType.TEXT_HTML);
    configurer.mediaType("js", MediaType.valueOf("application/javascript"));
  }

  @Bean
  public DefaultMustacheFactory mustacheFactory() {
    final DefaultMustacheFactory mustacheFactory = new DefaultMustacheFactory();
    mustacheFactory.setObjectHandler(new ReflectionObjectHandler() {

    });

    return mustacheFactory;
  }

  // @Override
  // public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
  // configurer.setDefaultTimeout(-1);
  // configurer.setTaskExecutor(asyncTaskExecutor());
  // }
  //
  // @Bean
  // public AsyncTaskExecutor asyncTaskExecutor() {
  // return new SimpleAsyncTaskExecutor("async");
  // }

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @PostConstruct
  public void postContruct() {
    om.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);
    om.configure(SerializationFeature.INDENT_OUTPUT, false);
  }

  @Bean
  public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
    return new ShallowEtagHeaderFilter();
  }

}
