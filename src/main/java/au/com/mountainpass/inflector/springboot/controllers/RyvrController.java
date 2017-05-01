package au.com.mountainpass.inflector.springboot.controllers;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ryvrs/{name}")
public class RyvrController {

    @Autowired
    private HtmlController htmlController;

    @Autowired
    private JsonController jsonController;

    @RequestMapping(method = RequestMethod.GET, produces = {
            "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getJson(final HttpServletRequest req,
            @PathVariable String name,
            @RequestParam(required = false) Long page)
            throws URISyntaxException {
        return jsonController.getRyvr(req, name,
                page == null ? -1l : page.longValue());
    }

    @RequestMapping(method = RequestMethod.GET, produces = {
            MediaType.TEXT_HTML_VALUE })
    public ResponseEntity<?> getHtml(final HttpServletRequest req,
            @PathVariable String name,
            @RequestParam(required = false) Long page)
            throws URISyntaxException {
        return htmlController.getRyvr(req, name,
                page == null ? -1l : page.longValue());
    }
}
