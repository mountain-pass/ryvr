package au.com.mountainpass.inflector.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.github.mustachejava.DefaultMustacheFactory;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // registry.addResourceHandler("/api-docsxx/**").addResourceLocations("/webjars/swagger-ui/2.2.10/");
        // registry.addResourceHandler("/api-docsxx/swagger.json").addResourceLocations("/api/v2/swagger.json");
    }

    @Bean
    public DefaultMustacheFactory mustacheFactory() {
        DefaultMustacheFactory mustacheFactory = new DefaultMustacheFactory();
        return mustacheFactory;
    }

    @Override
    public void configureContentNegotiation(
            ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.TEXT_HTML);
        configurer.ignoreAcceptHeader(false).favorPathExtension(false)
                .mediaType("json", MediaType.APPLICATION_JSON);
        configurer.mediaType("html", MediaType.TEXT_HTML);
    }

}
