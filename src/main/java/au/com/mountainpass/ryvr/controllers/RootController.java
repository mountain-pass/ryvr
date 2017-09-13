package au.com.mountainpass.ryvr.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.webjars.RequireJS;

@RestController
@RequestMapping("/")
public class RootController {
  @Autowired
  private HtmlController htmlController;

  @Autowired
  private JsonController jsonController;

  @Value("${spring.resources.cache-period:2592000}")
  private long cachePeriod;

  @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
  public void getJson(final HttpServletResponse res, final HttpServletRequest req)
      throws IOException {
    jsonController.getRoot(res, req);
  }

  @RequestMapping(method = RequestMethod.GET, produces = { MediaType.ALL_VALUE })
  public ResponseEntity<?> getHtml(final HttpServletRequest req) {
    return htmlController.getRoot(req);
  }

  @RequestMapping(value = "/webjars.js", method = RequestMethod.GET, produces = "application/javascript")
  public ResponseEntity<?> webjarjs() {
    String body = RequireJS.getSetupJavaScript("/webjars/");
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(new MediaType("application", "javascript"))
        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS)).varyBy(HttpHeaders.ACCEPT,
            HttpHeaders.ACCEPT_CHARSET, HttpHeaders.ACCEPT_ENCODING, HttpHeaders.ACCEPT_LANGUAGE)
        .eTag(Integer.toHexString(body.hashCode())).body(body);
  }

  @RequestMapping("/user")
  public Principal user(Principal user) {
    return user;
  }

}
