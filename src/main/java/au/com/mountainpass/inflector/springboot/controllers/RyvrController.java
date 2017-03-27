package au.com.mountainpass.inflector.springboot.controllers;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;

import io.swagger.inflector.models.RequestContext;

public interface RyvrController {

    public CompletableFuture<ResponseEntity<?>> getApiDocs(
            RequestContext request, String group);

    public CompletableFuture<ResponseEntity<?>> getRvyrsCollection(
            RequestContext request, Long page, String xRequestId, String accept,
            String cacheControl);

    public CompletableFuture<ResponseEntity<?>> getRoot(RequestContext request);

    public CompletableFuture<ResponseEntity<?>> getRyvr(RequestContext request,
            String ryvrName, String xRequestId, String accept,
            String cacheControl);

}
