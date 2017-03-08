package au.com.mountainpass.inflector.springboot.controllers;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.swagger.inflector.models.RequestContext;

@Component()
public class HtmlController implements RyvrContentController {

    @Override
    public CompletableFuture<ResponseEntity<?>> getApiDocs(
            RequestContext request, String group) {
        return CompletableFuture.supplyAsync(() -> {
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(URI
                            .create("/system/webjars/swagger-ui/2.2.10/index.html?url=/api-docs"))
                    .build();
        });
    }

    @Override
    public boolean isCompatible(MediaType type) {
        return MediaType.TEXT_HTML_TYPE.isCompatible(type);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE - 1;
    }

    @Override
    public CompletableFuture<ResponseEntity<?>> getRvyrs(RequestContext request,
            String group) {
        throw new NotImplementedException();
    }

}
