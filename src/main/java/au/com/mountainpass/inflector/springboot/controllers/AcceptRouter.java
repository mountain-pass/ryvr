package au.com.mountainpass.inflector.springboot.controllers;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;

@Controller
public class AcceptRouter implements RyvrController {

    @Autowired
    private List<RyvrContentController> controllers;

    private Map<Integer, RyvrController> controllerMap = new HashMap<>();

    @Override
    public ResponseContext getApiDocs(RequestContext request, String group) {
        try {
            return findContentController(request.getAcceptableMediaTypes())
                    .getApiDocs(request, group);
        } catch (NotAcceptableException e) {
            return notAcceptable();
        }
    }

    private RyvrController findContentController(
            List<MediaType> accepableTypes) {
        int hashCode = accepableTypes.hashCode();
        RyvrController controller = controllerMap.get(hashCode);
        if (controller == null) {
            for (MediaType type : accepableTypes) {
                Optional<RyvrContentController> compatibleController = controllers
                        .stream()
                        .filter(controler -> controler.isCompatible(type))
                        .findAny();
                if (compatibleController.isPresent()) {
                    controllerMap.put(hashCode, compatibleController.get());
                    return compatibleController.get();
                }
            }
            throw new NotAcceptableException();
        }
        return controller;
    }

    @Override
    public ResponseContext getRyvrsCollection(RequestContext request, Long page,
            String xRequestId, String accept, String cacheControl) {
        try {
            return findContentController(request.getAcceptableMediaTypes())
                    .getRyvrsCollection(request, page, xRequestId, accept,
                            cacheControl);
        } catch (NotAcceptableException e) {
            return notAcceptable();
        }
    }

    public ResponseContext notAcceptable() {
        ResponseContext responseContext = new ResponseContext();
        responseContext.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        return responseContext;
    }

    @Override
    public ResponseContext getRoot(RequestContext request) {
        try {
            return findContentController(request.getAcceptableMediaTypes())
                    .getRoot(request);
        } catch (NotAcceptableException e) {
            return notAcceptable();
        }
    }

    @Override
    public ResponseContext getRyvr(RequestContext request, String ryvrName,
            Long page, String xRequestId, String accept, String cacheControl)
            throws URISyntaxException {
        try {
            return findContentController(request.getAcceptableMediaTypes())
                    .getRyvr(request, ryvrName, page, xRequestId, accept,
                            cacheControl);
        } catch (NotAcceptableException e) {
            return notAcceptable();
        }
    }

}
