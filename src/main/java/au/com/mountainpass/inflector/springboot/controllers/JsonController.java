package au.com.mountainpass.inflector.springboot.controllers;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import au.com.mountainpass.ryvr.SwaggerFetcher;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import io.swagger.config.FilterFactory;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import io.swagger.inflector.utils.VendorSpecFilter;
import io.swagger.models.Swagger;

@Component()
public class JsonController implements RyvrContentController {

    private static final MediaType APPLICATION_YAML_TYPE = new MediaType(
            "application", "yaml");

    private static final MediaType APPLICATION_HAL_JSON_TYPE = new MediaType(
            "application", "hal+json");

    @Autowired
    private SwaggerFetcher swaggerFetcher;

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private RyvrsCollection ryvrsCollection;

    @Override
    public ResponseContext getApiDocs(RequestContext request, String group) {

        Swagger swagger = swaggerFetcher.getSwagger();
        SwaggerSpecFilter filter = FilterFactory.getFilter();
        if (filter != null) {
            Map<String, String> cookies = new HashMap<String, String>();
            MultivaluedMap<String, String> headers = request.getHeaders();

            swagger = new VendorSpecFilter().filter(swagger, filter, null,
                    cookies, headers);
        }
        return MainRyvrController
                .toResponseContext(ResponseEntity.status(HttpStatus.OK)
                        .contentType(
                                org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(swagger));
    }

    @Override
    public boolean isCompatible(MediaType type) {
        return APPLICATION_HAL_JSON_TYPE.isCompatible(type)
                || MediaType.APPLICATION_JSON_TYPE.isCompatible(type)
                || APPLICATION_YAML_TYPE.isCompatible(type);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public ResponseContext getRyvrsCollection(RequestContext request, Long page,
            String xRequestId, String accept, String cacheControl) {
        return MainRyvrController
                .toResponseContext(ResponseEntity.status(HttpStatus.OK)
                        .contentType(org.springframework.http.MediaType
                                .valueOf("application/hal+json"))
                        .body(ryvrsCollection));
    }

    @Override
    public ResponseContext getRoot(RequestContext request) {
        Root root = new Root(applicationName);
        return MainRyvrController
                .toResponseContext(
                        ResponseEntity.status(HttpStatus.OK)
                                .contentType(org.springframework.http.MediaType
                                        .valueOf("application/hal+json"))
                                .body(root));
    }

    @Override
    public ResponseContext getRyvr(RequestContext request, String ryvrName,
            String xRequestId, String accept, String cacheControl)
            throws URISyntaxException {

        Ryvr ryvr = ryvrsCollection.getRyvr(ryvrName);
        ryvr.refresh();
        return MainRyvrController
                .toResponseContext(
                        ResponseEntity.status(HttpStatus.OK)
                                .contentType(org.springframework.http.MediaType
                                        .valueOf("application/hal+json"))
                                .body(ryvr));

    }

}
