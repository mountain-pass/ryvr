package au.com.mountainpass.ryvr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.inflector.config.Configuration;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;

@Component
public class SwaggerFetcher {

    private Swagger swagger;

    public SwaggerFetcher(@Autowired Configuration config) {
        SwaggerDeserializationResult swaggerParseResult = new SwaggerParser()
                .readWithInfo(config.getSwaggerUrl(), null, true);
        this.swagger = swaggerParseResult.getSwagger();

    }

    public Swagger getSwagger() {
        return this.swagger;
    }
}
