package au.com.mountainpass.ryvr.model;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import au.com.mountainpass.ryvr.testclient.model.SwaggerImpl;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

@Component
public class JavaSwaggerImpl implements SwaggerImpl {

  private final String swagger;
  private final static SwaggerParser swaggerParser = new SwaggerParser();
  private Swagger parsedSwagger;

  public JavaSwaggerImpl() throws IOException {
    ClassPathResource index = new ClassPathResource("static/swagger.json");

    StringWriter writer = new StringWriter();
    IOUtils.copy(index.getInputStream(), writer, "UTF-8");
    swagger = writer.toString();
  }

  @Override
  public boolean containsOperation(String operationId) {
    return getParsedSwagger().getPaths().entrySet().stream()
        .filter(entry -> entry.getValue().getGet().getOperationId().equals(operationId)).findAny()
        .isPresent();
  }

  private Swagger getParsedSwagger() {
    if (parsedSwagger == null) {
      parsedSwagger = swaggerParser.parse(getSwagger());
    }
    return parsedSwagger;
  }

  public String getSwagger() {
    return swagger;
  }

}
