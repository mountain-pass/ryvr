package au.com.mountainpass.inflector.springboot.controllers;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;

public interface RyvrController {

    public CompletableFuture<ResponseEntity<?>> getApiDocs(
            io.swagger.inflector.models.RequestContext request, String group);

    public CompletableFuture<ResponseEntity<?>> getRvyrs(
            io.swagger.inflector.models.RequestContext request, String group);
}
