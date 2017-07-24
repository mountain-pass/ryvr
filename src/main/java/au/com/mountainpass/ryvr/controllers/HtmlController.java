package au.com.mountainpass.ryvr.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import au.com.mountainpass.ryvr.io.RyvrSerialiser;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

@Component()
public class HtmlController {

  @Autowired
  private DefaultMustacheFactory mustacheFactory;

  @Autowired
  private JsonController jsonController;

  @Autowired
  private RyvrsCollection ryvrsCollection;

  @Autowired
  private RyvrSerialiser serialiser;

  @Autowired
  private ObjectMapper om;

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("${au.com.mountainpass.ryvr.cache.archive-page-max-age}")
  private long archivePageMaxAge;

  @Value("${au.com.mountainpass.ryvr.cache.archive-page-max-age-unit}")
  private TimeUnit archivePageMaxAgeUnit;

  @Value("${au.com.mountainpass.ryvr.cache.current-page-max-age}")
  private long currentPageMaxAge;

  @Value("${au.com.mountainpass.ryvr.cache.current-page-max-age-unit}")
  private TimeUnit currentPageMaxAgeUnit;

  public ResponseEntity<?> getApiDocs(HttpServletRequest req, String group) {
    return ResponseEntity.status(HttpStatus.SEE_OTHER)
        .location(URI.create("/webjars/swagger-ui/2.2.10/index.html?url=/api-docs")).build();
  }

  public ResponseEntity<?> getRyvrsCollection(HttpServletRequest req) {
    Root root = (Root) jsonController.getRoot(req).getBody();
    RyvrsCollection collection = (RyvrsCollection) jsonController.getRyvrsCollection(req).getBody();
    return getIndex(root, collection, null);
  }

  public ResponseEntity<?> getRoot(HttpServletRequest req) {
    Root root = (Root) jsonController.getRoot(req).getBody();
    return getIndex(root, root, null);
  }

  private ResponseEntity<?> getIndex(Root root, Object resource, HttpHeaders headers) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ClassPathResource index = new ClassPathResource("static/index.html");
    try {
      Mustache mustache = mustacheFactory.compile(new InputStreamReader(index.getInputStream()),
          "static/index.html", "<%", "%>");
      Map<String, String> scope = new HashMap<>();
      scope.put("root", om.writeValueAsString(root));
      // scope.put("root-links", TODO);
      if (resource instanceof String) {
        String body = (String) resource;
        scope.put("resource", body);
        scope.put("resource-headers", om.writeValueAsString(headers));
      } else {
        scope.put("resource", om.writeValueAsString(resource));
        scope.put("resource-headers", "{}");
      }

      OutputStreamWriter writer = new OutputStreamWriter(baos);
      mustache.execute(writer, scope).flush();
      writer.flush();

      return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(baos.toByteArray());
    } catch (IOException e) {
      throw new NotImplementedException(e);
    }
  }

  public ResponseEntity<?> getRyvr(HttpServletRequest req, String ryvrName, long page)
      throws URISyntaxException, IOException {

    Ryvr ryvr = ryvrsCollection.getRyvr(ryvrName);
    if (ryvr == null) {
      return ResponseEntity.notFound().build();
    }

    Root root = (Root) jsonController.getRoot(req).getBody();

    MockHttpServletResponse rvyrRes = new MockHttpServletResponse();
    jsonController.getRyvr(rvyrRes, req, ryvrName, page);
    HttpHeaders headers = new HttpHeaders();
    for (Iterator<String> i = rvyrRes.getHeaderNames().iterator(); i.hasNext();) {
      String headerName = i.next();
      headers.put(headerName, rvyrRes.getHeaders(headerName));
    }
    return getIndex(root, rvyrRes.getContentAsString(), headers);
  }

}
