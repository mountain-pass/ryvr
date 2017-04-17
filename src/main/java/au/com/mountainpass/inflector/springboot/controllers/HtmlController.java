package au.com.mountainpass.inflector.springboot.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;

@Component()
public class HtmlController implements RyvrContentController {

    @Autowired
    DefaultMustacheFactory mustacheFactory;

    @Autowired
    JsonController jsonController;

    @Autowired
    ObjectMapper om;

    @Override
    public ResponseContext getApiDocs(RequestContext request, String group) {
        return MainRyvrController
                .toResponseContext(ResponseEntity.status(HttpStatus.SEE_OTHER)
                        .location(URI
                                .create("/system/webjars/swagger-ui/2.2.10/index.html?url=/api-docs"))
                        .build());
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
    public ResponseContext getRyvrsCollection(RequestContext request, Long page,
            String xRequestId, String accept, String cacheControl) {
        Root root = (Root) jsonController.getRoot(request).getEntity();
        RyvrsCollection collection = (RyvrsCollection) jsonController
                .getRyvrsCollection(request, page, xRequestId, accept,
                        cacheControl)
                .getEntity();
        return getIndex(root, collection);
    }

    @Override
    public ResponseContext getRoot(RequestContext request) {
        Root root = (Root) jsonController.getRoot(request).getEntity();
        return getIndex(root, root);
    }

    private ResponseContext getIndex(Root root, Object resource) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ClassPathResource index = new ClassPathResource("static/index.html");
        try {
            Mustache mustache = mustacheFactory.compile(
                    new InputStreamReader(index.getInputStream()),
                    "static/index.html", "<%", "%>");
            Map<String, String> scope = new HashMap<>();
            scope.put("root", om.writeValueAsString(root));
            scope.put("resource", om.writeValueAsString(resource));
            OutputStreamWriter writer = new OutputStreamWriter(baos);
            mustache.execute(writer, scope).flush();
            writer.flush();
            return MainRyvrController.toResponseContext(ResponseEntity
                    .ok(new ByteArrayInputStream(baos.toByteArray())));
        } catch (IOException e) {
            throw new NotImplementedException(e);
        }
    }

    @Override
    public ResponseContext getRyvr(RequestContext request, String ryvrName,
            String xRequestId, String accept, String cacheControl)
            throws URISyntaxException {
        Root root = (Root) jsonController.getRoot(request).getEntity();
        Ryvr ryvr = (Ryvr) jsonController
                .getRyvr(request, ryvrName, xRequestId, accept, cacheControl)
                .getEntity();
        return getIndex(root, ryvr);
    }

}
