package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.stream.Collectors;

import io.swagger.models.Swagger;

public class JavaSwaggerResponse implements SwaggerResponse {

    private Swagger swagger;

    public JavaSwaggerResponse(Swagger swagger) {
        this.swagger = swagger;
    }

    @Override
    public void assertHasGetApiDocsOperation() {
        assertThat(swagger.getPaths().entrySet().stream().map(entry -> {
            return entry.getValue().getGet().getOperationId();
        }).collect(Collectors.toList()), hasItem("getApiDocs"));
    }

}
