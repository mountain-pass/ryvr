package au.com.mountainpass.inflector.springboot.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.mountainpass.ryvr.SwaggerFetcher;
import au.com.mountainpass.ryvr.model.Root;
import io.swagger.config.FilterFactory;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.utils.VendorSpecFilter;
import io.swagger.models.Swagger;

@Component()
public class JsonController implements RyvrContentController {

    private static final MediaType APPLICATION_YAML_TYPE = new MediaType(
            "application", "yaml");
    @Autowired
    SwaggerFetcher swaggerFetcher;

    @Override
    public CompletableFuture<ResponseEntity<?>> getApiDocs(
            RequestContext request, String group) {

        return CompletableFuture.supplyAsync(() -> {
            Swagger swagger = swaggerFetcher.getSwagger();
            SwaggerSpecFilter filter = FilterFactory.getFilter();
            if (filter != null) {
                Map<String, String> cookies = new HashMap<String, String>();
                MultivaluedMap<String, String> headers = request.getHeaders();

                swagger = new VendorSpecFilter().filter(swagger, filter, null,
                        cookies, headers);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(
                            org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(swagger);
        });
    }

    @Override
    public boolean isCompatible(MediaType type) {
        return MediaType.APPLICATION_JSON_TYPE.isCompatible(type)
                || APPLICATION_YAML_TYPE.isCompatible(type);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public CompletableFuture<ResponseEntity<?>> getRvyrs(
            RequestContext request) {
        throw new NotImplementedException();
    }

    @Override
    public CompletableFuture<ResponseEntity<?>> getRoot(
            RequestContext request) {
        return CompletableFuture.supplyAsync(() -> {
            Root root = new Root();
            final ObjectMapper mapper = new ObjectMapper();

            try {
                final String json = mapper.writeValueAsString(root);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(org.springframework.http.MediaType
                            .valueOf("application/hal+json"))
                    .body(root);
        });
    }

}
