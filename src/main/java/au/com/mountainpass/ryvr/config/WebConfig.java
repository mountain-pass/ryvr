package au.com.mountainpass.ryvr.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
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
  public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
    configurer.defaultContentType(MediaType.TEXT_HTML);
    configurer.ignoreAcceptHeader(false).favorPathExtension(false).mediaType("json",
        MediaType.APPLICATION_JSON);
    configurer.mediaType("html", MediaType.TEXT_HTML);
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

  @PostConstruct
  public void postContruct() {
    om.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);
    om.configure(SerializationFeature.INDENT_OUTPUT, false);
  }

}
