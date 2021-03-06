package au.com.mountainpass.ryvr.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

  @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
  public void getJson(HttpServletResponse res, final HttpServletRequest req) throws IOException {
    jsonController.getRyvrsCollection(res, req);
  }

  @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE })
  public void getHtml(HttpServletResponse res, final HttpServletRequest req) throws IOException {
    htmlController.getRyvrsCollection(res, req);
  }
}
