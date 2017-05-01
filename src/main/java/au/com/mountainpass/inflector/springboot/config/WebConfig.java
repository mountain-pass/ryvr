package au.com.mountainpass.inflector.springboot.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.reflect.ReflectionObjectHandler;

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
        mustacheFactory.setObjectHandler(new ReflectionObjectHandler() {
            // @Override
            // protected boolean areMethodsAccessible(Map<?, ?> map) {
            // return true;
            // }
            //
            // @Override
            // public Object coerce(Object object) {
            // if (object instanceof Collection) {
            // return new DecoratedCollection((Collection) object);
            // }
            // return super.coerce(object);
            // }
            //
            // @Override
            // public String stringify(Object object) {
            // if (object instanceof String) {
            // return "\"" + ((String) object) + "\"";
            // } else {
            // return object.toString();
            // }
            // }
        });

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

    @Autowired
    private ObjectMapper om;

    @PostConstruct
    public void postContruct() {
        om.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);
        om.configure(SerializationFeature.INDENT_OUTPUT, false);
    }

}
