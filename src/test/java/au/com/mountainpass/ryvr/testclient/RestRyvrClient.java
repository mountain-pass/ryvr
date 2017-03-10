package au.com.mountainpass.ryvr.testclient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestTemplate;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.testclient.model.JavaSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import io.swagger.parser.SwaggerParser;

public class RestRyvrClient implements RyvrTestClient {

    @Autowired
    AsyncRestTemplate restTemplate;

    @Autowired
    RyvrConfiguration config;

    SwaggerParser swaggerParser = new SwaggerParser();

    @Override
    public CompletableFuture<SwaggerResponse> getApiDocs() {
        URI url = config.getBaseUri().resolve("/api-docs");

        CompletableFuture<ResponseEntity<String>> rval = FutureConverter
                .convert(restTemplate.getForEntity(url, String.class));
        return rval.thenApply(response -> {
            return new JavaSwaggerResponse(
                    swaggerParser.parse(response.getBody()));
        });
    }

}
