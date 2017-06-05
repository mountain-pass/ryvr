package au.com.mountainpass.ryvr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebConfigSupport extends WebMvcConfigurationSupport {

  // @Override
  // protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
  // converters.add(new CsvMessageWriter('\t', '"'));
  // addDefaultHttpMessageConverters(converters);
  // }

}
