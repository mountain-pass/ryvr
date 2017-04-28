package au.com.mountainpass.inflector.springboot.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-docs")
public class ApiDocsController {

    @Autowired
    private HtmlController htmlController;

    @Autowired
    private JsonController jsonController;

    @RequestMapping(method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getJson(final HttpServletRequest req,
            @RequestParam(required = false) String group) {
        return jsonController.getApiDocs(req, group);
    }

    @RequestMapping(method = RequestMethod.GET, produces = {
            MediaType.TEXT_HTML_VALUE })
    public ResponseEntity<?> getHtml(final HttpServletRequest req,
            @RequestParam(required = false) String group) {
        return htmlController.getApiDocs(req, group);
    }
}
