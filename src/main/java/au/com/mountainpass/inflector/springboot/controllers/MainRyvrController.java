package au.com.mountainpass.inflector.springboot.controllers;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;

@Component
public class MainRyvrController implements RyvrController {
    private Logger logger = LoggerFactory.getLogger(MainRyvrController.class);

    @Autowired
    AcceptRouter router;

    NotAcceptableController notAcceptableController = new NotAcceptableController();

    @Override
    public ResponseContext getApiDocs(
            io.swagger.inflector.models.RequestContext request, String group) {
        return router.getApiDocs(request, group);
    }

    @Override
    public ResponseContext getRyvrsCollection(RequestContext request, Long page,
            String xRequestId, String accept, String cacheControl) {
        return router.getRyvrsCollection(request, page, xRequestId, accept,
                cacheControl);
    }

    @Override
    public ResponseContext getRoot(RequestContext request) {
        return router.getRoot(request);
    }

    @Override
    public io.swagger.inflector.models.ResponseContext getRyvr(
            io.swagger.inflector.models.RequestContext request, String ryvrName,
            Long page, String xRequestId, String accept, String cacheControl)
            throws URISyntaxException {
        return router.getRyvr(request, ryvrName, page, xRequestId, accept,
                cacheControl);
    }

    public static ResponseContext toResponseContext(
            ResponseEntity<?> response) {
        ResponseContext rval = new ResponseContext();
        rval.setStatus(response.getStatusCodeValue());
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        for (Map.Entry<String, List<String>> entry : response.getHeaders()
                .entrySet()) {
            headers.addAll(entry.getKey(), entry.getValue());
        }
        rval.setHeaders(headers);
        rval.setEntity(response.getBody());
        return rval;
    }

}
