package au.com.mountainpass.ryvr.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

@Component()
public class HtmlController {

  @Autowired
  private DefaultMustacheFactory mustacheFactory;

  @Autowired
  private JsonController jsonController;

  @Autowired
  private ObjectMapper om;

  public ResponseEntity<?> getApiDocs(HttpServletRequest req, String group) {
    return ResponseEntity.status(HttpStatus.SEE_OTHER)
        .location(URI.create("/webjars/swagger-ui/2.2.10/index.html?url=/api-docs")).build();
  }

  public ResponseEntity<?> getRyvrsCollection(HttpServletRequest req) {
    Root root = (Root) jsonController.getRoot(req).getBody();
    RyvrsCollection collection = (RyvrsCollection) jsonController.getRyvrsCollection(req).getBody();
    return getIndex(root, collection);
  }

  public ResponseEntity<?> getRoot(HttpServletRequest req) {
    Root root = (Root) jsonController.getRoot(req).getBody();
    return getIndex(root, root);
  }

  private ResponseEntity<?> getIndex(Root root, Object resource) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ClassPathResource index = new ClassPathResource("static/index.html");
    try {
      Mustache mustache = mustacheFactory.compile(new InputStreamReader(index.getInputStream()),
          "static/index.html", "<%", "%>");
      Map<String, String> scope = new HashMap<>();
      scope.put("root", om.writeValueAsString(root));
      scope.put("resource", om.writeValueAsString(resource));
      OutputStreamWriter writer = new OutputStreamWriter(baos);
      mustache.execute(writer, scope).flush();
      writer.flush();

      return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(baos.toByteArray());
    } catch (IOException e) {
      throw new NotImplementedException(e);
    }
  }

  public void getRyvr(HttpServletRequest req, Writer responseWriter, String ryvrName, Long page)
      throws URISyntaxException {
    throw new NotImplementedException();
    // Root root = (Root) jsonController.getRoot(req).getBody();
    // Ryvr ryvr = (Ryvr) jsonController.getRyvr(req, responseWriter, ryvrName, page).getBody();
    // return getIndex(root, ryvr);
  }

}
