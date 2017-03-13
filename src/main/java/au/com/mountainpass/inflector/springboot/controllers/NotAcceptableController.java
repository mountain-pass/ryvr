package au.com.mountainpass.inflector.springboot.controllers;

import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MediaType;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.inflector.models.RequestContext;

public class NotAcceptableController implements RyvrContentController {

    @Override
    public CompletableFuture<ResponseEntity<?>> getApiDocs(
            RequestContext request, String group) {
        return notAcceptable();
    }

    @Override
    public boolean isCompatible(MediaType type) {
        return true;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public CompletableFuture<ResponseEntity<?>> getRvyrs(
            RequestContext request) {
        return notAcceptable();
    }

    private CompletableFuture<ResponseEntity<?>> notAcceptable() {
        return CompletableFuture.supplyAsync(() -> {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        });
    }

    @Override
    public CompletableFuture<ResponseEntity<?>> getRoot(
            RequestContext request) {
        return notAcceptable();
    }

}
