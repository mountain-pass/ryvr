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
public class YamlController implements RyvrContentController {

    @Override
    public CompletableFuture<ResponseEntity<?>> getApiDocs(
            RequestContext request, String group) {
        return CompletableFuture.supplyAsync(() -> {
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .location(URI.create("/swagger.json")).build();
        });
    }

    @Override
    public boolean isCompatible(MediaType type) {
        return new MediaType("application", "yaml").isCompatible(type);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public CompletableFuture<ResponseEntity<?>> getRvyrs(RequestContext request,
            String group) {
        throw new NotImplementedException();
    }

    @Override
    public CompletableFuture<ResponseEntity<?>> getRoot(
            RequestContext request) {
        throw new NotImplementedException();
    }

}
