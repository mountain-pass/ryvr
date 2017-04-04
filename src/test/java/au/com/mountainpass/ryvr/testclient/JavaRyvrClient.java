package au.com.mountainpass.ryvr.testclient;

import java.util.Collections;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import au.com.mountainpass.inflector.springboot.controllers.AcceptRouter;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.testclient.model.JavaRootResponse;
import au.com.mountainpass.ryvr.testclient.model.JavaSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;
import io.swagger.inflector.models.RequestContext;
import io.swagger.models.Swagger;

public class JavaRyvrClient implements RyvrTestClient {

    @Autowired
    AcceptRouter router;

    @Override
    public SwaggerResponse getApiDocs() {
        RequestContext request = new RequestContext();
        request.setAcceptableMediaTypes(
                Collections.singletonList(MediaType.APPLICATION_JSON_TYPE));
        return new JavaSwaggerResponse(
                (Swagger) router.getApiDocs(request, "").getBody());
    }

    @Override
    public RootResponse getRoot() {
        RequestContext request = new RequestContext();
        request.setAcceptableMediaTypes(
                Collections.singletonList(MediaType.APPLICATION_JSON_TYPE));
        return new JavaRootResponse((Root) router.getRoot(request).getBody(),
                router);
    }

    @Override
    public RyvrsCollectionResponse getRyvrsCollection() {
        return getRoot().followRyvrsLink();
    }

    @Override
    public RyvrResponse getRyvr(String name) {
        return getRyvrsCollection().followEmbeddedRyvrLink(name);
    }

    @Override
    public void after(Scenario s) {
        // TODO, embed java response here
    }

}
