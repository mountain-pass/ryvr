package au.com.mountainpass.inflector.springboot.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RootController {
    @Autowired
    private HtmlController htmlController;

    @Autowired
    private JsonController jsonController;

    @RequestMapping(method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getJson(final HttpServletRequest req) {
        return jsonController.getRoot(req);
    }

    @RequestMapping(method = RequestMethod.GET, produces = {
            MediaType.ALL_VALUE })
    public ResponseEntity<?> getHtml(final HttpServletRequest req) {
        return htmlController.getRoot(req);
    }
}
