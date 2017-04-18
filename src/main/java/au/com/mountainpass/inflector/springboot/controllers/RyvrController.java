package au.com.mountainpass.inflector.springboot.controllers;

import java.net.URISyntaxException;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;

public interface RyvrController {

    public ResponseContext getApiDocs(RequestContext request, String group);

    public ResponseContext getRyvrsCollection(RequestContext request, Long page,
            String xRequestId, String accept, String cacheControl);

    public ResponseContext getRoot(RequestContext request);

    public ResponseContext getRyvr(RequestContext request, String ryvrName,
            Long page, String xRequestId, String accept, String cacheControl)
            throws URISyntaxException;

}
