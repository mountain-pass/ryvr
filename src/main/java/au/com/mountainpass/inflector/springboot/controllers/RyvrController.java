package au.com.mountainpass.inflector.springboot.controllers;

import org.springframework.http.ResponseEntity;

import io.swagger.inflector.models.RequestContext;

public interface RyvrController {

    public ResponseEntity<?> getApiDocs(RequestContext request, String group);

    public ResponseEntity<?> getRvyrsCollection(RequestContext request,
            Long page, String xRequestId, String accept, String cacheControl);

    public ResponseEntity<?> getRoot(RequestContext request);

    public ResponseEntity<?> getRyvr(RequestContext request, String ryvrName,
            String xRequestId, String accept, String cacheControl);

}
