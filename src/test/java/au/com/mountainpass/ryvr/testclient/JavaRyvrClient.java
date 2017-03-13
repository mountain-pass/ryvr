package au.com.mountainpass.ryvr.testclient;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import au.com.mountainpass.inflector.springboot.controllers.AcceptRouter;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.testclient.model.JavaRootResponse;
import au.com.mountainpass.ryvr.testclient.model.JavaSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import io.swagger.inflector.models.RequestContext;
import io.swagger.models.Swagger;

public class JavaRyvrClient implements RyvrTestClient {

    @Autowired
    AcceptRouter router;

    @Override
    public CompletableFuture<SwaggerResponse> getApiDocs() {
        RequestContext request = new RequestContext();
        request.setAcceptableMediaTypes(
                Collections.singletonList(MediaType.APPLICATION_JSON_TYPE));
        return router.getApiDocs(request, "").thenApply(response -> {
            return new JavaSwaggerResponse((Swagger) response.getBody());
        });
    }

    @Override
    public CompletableFuture<RootResponse> getRoot() {
        RequestContext request = new RequestContext();
        request.setAcceptableMediaTypes(
                Collections.singletonList(MediaType.APPLICATION_JSON_TYPE));
        return router.getRoot(request).thenApply(response -> {
            return new JavaRootResponse((Root) response.getBody());
        });
    }

}
