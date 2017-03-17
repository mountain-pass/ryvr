package au.com.mountainpass.inflector.springboot.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.TemplateContext;

import io.swagger.inflector.models.RequestContext;

@Component()
public class HtmlController implements RyvrContentController {

    @Autowired
    DefaultMustacheFactory mustacheFactory;

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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ClassPathResource index = new ClassPathResource("static/index.html");
        try {
            Mustache mustache = mustacheFactory.compile(
                    new InputStreamReader(index.getInputStream()),
                    "static/index.html", "<%", "%>");
            TemplateContext context = new TemplateContext("<%", "%>",
                    "static/index.html", 0, false);
            OutputStreamWriter writer = new OutputStreamWriter(baos);
            mustache.execute(writer, context).flush();
            writer.flush();
            return CompletableFuture.supplyAsync(() -> {
                return ResponseEntity
                        .ok(new ByteArrayInputStream(baos.toByteArray()));
            });
        } catch (IOException e) {
            throw new NotImplementedException(e);
        }
        // return CompletableFuture.supplyAsync(() -> {
        // try {
        // return ResponseEntity.ok(index.getInputStream());
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // throw new NotImplementedException(e);
        // }
        // });
    }

}
