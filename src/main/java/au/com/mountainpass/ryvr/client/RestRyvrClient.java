package au.com.mountainpass.ryvr.client;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestTemplate;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import io.swagger.parser.SwaggerParser;

public class RestRyvrClient implements RyvrClient {

    @Autowired
    AsyncRestTemplate restTemplate;

    @Autowired
    RyvrConfiguration config;

    SwaggerParser swaggerParser = new SwaggerParser();

    @Override
    public CompletableFuture<ResponseEntity<?>> getApiDocs(MediaType resource) {
        URI url = config.getBaseUri().resolve("/api-docs");

        CompletableFuture<ResponseEntity<String>> rval = FutureConverter
                .convert(restTemplate.getForEntity(url, String.class));
        return rval.thenApply(response -> {
            return new ResponseEntity<>(swaggerParser.parse(response.getBody()),
                    response.getHeaders(), response.getStatusCode());
        });
    }

}
