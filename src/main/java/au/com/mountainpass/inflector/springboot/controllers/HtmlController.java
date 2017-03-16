package au.com.mountainpass.inflector.springboot.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
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
    public CompletableFuture<ResponseEntity<?>> getRvyrsCollection(
            RequestContext request, Long page, String xRequestId, String accept,
            String cacheControl) {
        return getIndex();
    }

    @Override
    public CompletableFuture<ResponseEntity<?>> getRoot(
            RequestContext request) {
        return getIndex();
    }

    private CompletableFuture<ResponseEntity<?>> getIndex() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ClassPathResource index = new ClassPathResource(
                        "static/index.html");
                InputStream indexStream = index.getInputStream();
                return ResponseEntity.ok(indexStream);
            } catch (IOException e) {
                throw new NotImplementedException();
            }
        });
    }

}
