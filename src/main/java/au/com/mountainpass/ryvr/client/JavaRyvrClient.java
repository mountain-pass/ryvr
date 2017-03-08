package au.com.mountainpass.ryvr.client;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import au.com.mountainpass.inflector.springboot.controllers.AcceptRouter;
import io.swagger.inflector.models.RequestContext;

public class JavaRyvrClient implements RyvrClient {

    @Autowired
    AcceptRouter router;

    @Override
    public CompletableFuture<ResponseEntity<?>> getApiDocs(
            javax.ws.rs.core.MediaType mediaType) {
        RequestContext request = new RequestContext();
        request.setAcceptableMediaTypes(Collections.singletonList(mediaType));
        return router.getApiDocs(request, "");
    }

}
