package au.com.mountainpass.ryvr.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ryvrs")
public class RyvrsCollectionController {

    @Autowired
    private HtmlController htmlController;

    @Autowired
    private JsonController jsonController;

    @RequestMapping(method = RequestMethod.GET, produces = {
            "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getJson(final HttpServletRequest req) {
        return jsonController.getRyvrsCollection(req);
    }

    @RequestMapping(method = RequestMethod.GET, produces = {
            MediaType.TEXT_HTML_VALUE })
    public ResponseEntity<?> getHtml(final HttpServletRequest req) {
        return htmlController.getRyvrsCollection(req);
    }
}
