package au.com.mountainpass.ryvr.controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;

@RestController
@RequestMapping("/ryvrs/{name}")
public class RyvrController {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private HtmlController htmlController;

  @Autowired
  private JsonController jsonController;

  @Autowired
  private RyvrConfiguration config;

  @RequestMapping(method = RequestMethod.GET, produces = { "application/hal+json",
      MediaType.APPLICATION_JSON_VALUE })
  public void getJson(final HttpServletResponse res, final HttpServletRequest req,
      @PathVariable String name, @RequestParam(required = false) Long page)
      throws URISyntaxException, IOException {
    if (page == null) {
      // jsonController.getRyvr(res, req, name);
      res.setStatus(HttpStatus.PERMANENT_REDIRECT.value());
      res.addHeader(HttpHeaders.LOCATION,
          config.getBaseUri().resolve("/ryvrs/" + name + "?page=1").toString());
    } else {
      if (page <= 0L) {
        res.setStatus(HttpStatus.NOT_FOUND.value());
      } else {
        jsonController.getRyvr(res, req, name, page);
      }
    }
  }

  @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE })
  public ResponseEntity<?> getHtml(HttpServletResponse res, final HttpServletRequest req,
      @PathVariable String name, @RequestParam(required = false) Long page)
      throws URISyntaxException, IOException {
    if (page == null) {
      return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
          .location(config.getBaseUri().resolve("/ryvrs/" + name + "?page=1")).build();
    } else {
      if (page <= 0L) {
        throw new ResourceNotFoundException(req);
      } else {
        return htmlController.getRyvr(res, req, name, page);
      }
    }
  }

  // @ExceptionHandler(Exception.class)
  // public ResponseEntity<?> handleException(Exception exception, HttpServletRequest request) {
  // LOGGER.error("Naaggghhh!", exception);
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  // }
}
