package au.com.mountainpass.inflector.springboot.controllers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
public class MainRyvrController {
    private Logger logger = LoggerFactory.getLogger(MainRyvrController.class);

    @Autowired
    AcceptRouter router;

    NotAcceptableController notAcceptableController = new NotAcceptableController();

    public ResponseContext getApiDocs(
            io.swagger.inflector.models.RequestContext request, String group)
            throws InterruptedException, ExecutionException {
        CompletableFuture<ResponseEntity<?>> result = router.getApiDocs(request,
                group);
        return toResponseContext(result);
    }

    public ResponseContext getRvyrsCollection(RequestContext request, Long page,
            String xRequestId, String accept, String cacheControl)
            throws InterruptedException, ExecutionException {
        CompletableFuture<ResponseEntity<?>> result = router.getRvyrsCollection(
                request, page, xRequestId, accept, cacheControl);
        return toResponseContext(result);
    }

    public ResponseContext getRoot(RequestContext request)
            throws InterruptedException, ExecutionException {
        CompletableFuture<ResponseEntity<?>> result = router.getRoot(request);
        return toResponseContext(result);
    }

    private ResponseContext toResponseContext(
            CompletableFuture<ResponseEntity<?>> result)
            throws InterruptedException, ExecutionException {
        ResponseEntity<?> response = result.get();
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
