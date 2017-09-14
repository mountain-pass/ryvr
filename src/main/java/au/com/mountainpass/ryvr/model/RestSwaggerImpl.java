package au.com.mountainpass.ryvr.model;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.models.Swagger;
import io.swagger.parser.Swagger20Parser;

public class RestSwaggerImpl implements SwaggerImpl {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestSwaggerImpl.class);

  private Swagger swagger;
  private static Swagger20Parser swaggerParser = new Swagger20Parser();

  public RestSwaggerImpl(CloseableHttpResponse response) throws IOException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
    LOGGER.info(writer.toString());
    swagger = swaggerParser.parse(writer.toString());
  }

  @Override
  public boolean containsOperation(String operationId) {
    return swagger.getPaths().entrySet().stream()
        .filter(entry -> entry.getValue().getGet().getOperationId().equals(operationId)).findAny()
        .isPresent();
  }

  public Swagger getSwagger() {
    return swagger;
  }

}
