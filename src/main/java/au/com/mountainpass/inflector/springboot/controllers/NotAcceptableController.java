package au.com.mountainpass.inflector.springboot.controllers;

import javax.ws.rs.core.MediaType;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.inflector.models.RequestContext;

public class NotAcceptableController implements RyvrContentController {

    @Override
    public ResponseEntity<?> getApiDocs(RequestContext request, String group) {
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
    public ResponseEntity<?> getRvyrsCollection(RequestContext request,
            Long page, String xRequestId, String accept, String cacheControl) {
        return notAcceptable();
    }

    private ResponseEntity<?> notAcceptable() {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @Override
    public ResponseEntity<?> getRoot(RequestContext request) {
        return notAcceptable();
    }

    @Override
    public ResponseEntity<?> getRyvr(RequestContext request, String ryvrName,
            String xRequestId, String accept, String cacheControl) {
        return notAcceptable();
    }

}
