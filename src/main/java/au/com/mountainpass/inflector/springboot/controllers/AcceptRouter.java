package au.com.mountainpass.inflector.springboot.controllers;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;

@Controller
public class AcceptRouter implements RyvrController {

    @Autowired
    private List<RyvrContentController> controllers;

    private NotAcceptableController notAcceptableController = new NotAcceptableController();

    @Override
    public ResponseContext getApiDocs(RequestContext request, String group) {
        return findContentController(request.getAcceptableMediaTypes())
                .getApiDocs(request, group);
    }

    private RyvrController findContentController(
            List<MediaType> accepableTypes) {
        for (MediaType type : accepableTypes) {
            Optional<RyvrContentController> compatibleController = controllers
                    .stream().filter(controler -> controler.isCompatible(type))
                    .findAny();
            if (compatibleController.isPresent()) {
                return compatibleController.get();
            }
        }
        return notAcceptableController;
    }

    @Override
    public ResponseContext getRyvrsCollection(RequestContext request, Long page,
            String xRequestId, String accept, String cacheControl) {
        return findContentController(request.getAcceptableMediaTypes())
                .getRyvrsCollection(request, page, xRequestId, accept,
                        cacheControl);
    }

    @Override
    public ResponseContext getRoot(RequestContext request) {
        return findContentController(request.getAcceptableMediaTypes())
                .getRoot(request);
    }

    @Override
    public ResponseContext getRyvr(RequestContext request, String ryvrName,
            Long page, String xRequestId, String accept, String cacheControl)
            throws URISyntaxException {
        return findContentController(request.getAcceptableMediaTypes()).getRyvr(
                request, ryvrName, page, xRequestId, accept, cacheControl);
    }

}
