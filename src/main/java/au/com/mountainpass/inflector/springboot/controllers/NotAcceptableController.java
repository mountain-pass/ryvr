package au.com.mountainpass.inflector.springboot.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;

public class NotAcceptableController implements RyvrController {

    @Override
    public ResponseContext getApiDocs(RequestContext request, String group) {
        return notAcceptable();
    }

    @Override
    public ResponseContext getRyvrsCollection(RequestContext request, Long page,
            String xRequestId, String accept, String cacheControl) {
        return notAcceptable();
    }

    private ResponseContext notAcceptable() {
        return MainRyvrController.toResponseContext(
                ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build());
    }

    @Override
    public ResponseContext getRoot(RequestContext request) {
        return notAcceptable();
    }

    @Override
    public ResponseContext getRyvr(RequestContext request, String ryvrName,
            String xRequestId, String accept, String cacheControl) {
        return notAcceptable();
    }

}
