package au.com.mountainpass.inflector.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // registry.addResourceHandler("/api-docsxx/**").addResourceLocations("/webjars/swagger-ui/2.2.10/");
        // registry.addResourceHandler("/api-docsxx/swagger.json").addResourceLocations("/api/v2/swagger.json");
    }

}
