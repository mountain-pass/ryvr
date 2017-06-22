package au.com.mountainpass.ryvr.controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/ryvrs/{name}")
public class RyvrController {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private HtmlController htmlController;

  @Autowired
  private JsonController jsonController;

  @RequestMapping(method = RequestMethod.GET, produces = { "application/hal+json",
      MediaType.APPLICATION_JSON_VALUE })
  public ResponseEntity<StreamingResponseBody> getJson(final HttpServletRequest req,
      @PathVariable String name, @RequestParam(required = false) Long page)
      throws URISyntaxException, IOException {
    if (page == null) {
      return jsonController.getRyvr(req, name);
    } else {
      return jsonController.getRyvr(req, name, page);
    }
  }

  @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE })
  public ResponseEntity<?> getHtml(final HttpServletRequest req, @PathVariable String name,
      @RequestParam(required = false) Long page) throws URISyntaxException {
    return htmlController.getRyvr(req, name, page == null ? -1l : page.longValue());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(Exception exception, HttpServletRequest request) {
    LOGGER.error("Naaggghhh!", exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
