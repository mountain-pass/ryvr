package au.com.mountainpass.ryvr.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.webjars.WebJarAssetLocator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import au.com.mountainpass.ryvr.io.RyvrSerialiser;
import au.com.mountainpass.ryvr.model.RyvrRoot;
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

  private String swaggerUiVersion;

  @Autowired
  private RyvrRoot root;

  @PostConstruct
  private void postConstruct() {
    Map<String, String> webJars = new WebJarAssetLocator().getWebJars();
    this.swaggerUiVersion = webJars.get("swagger-ui");
  }

  public ResponseEntity<?> getApiDocs(HttpServletRequest req, String group) {
    return ResponseEntity.status(HttpStatus.SEE_OTHER)
        .location(
            URI.create("/webjars/swagger-ui/" + swaggerUiVersion + "/index.html?url=/api-docs"))
        .build();
  }

  public void getRyvrsCollection(final HttpServletResponse res, HttpServletRequest req)
      throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serialiser.toJson(root, baos);
    String serializedRoot = baos.toString();
    res.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);
    JsonController.addLinks(root, res);
    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
    serialiser.toJson(ryvrsCollection, baos2);
    String serializedRyvrsCollection = baos2.toString();
    HttpHeaders resourceHeaders = new HttpHeaders();
    JsonController.addLinks(ryvrsCollection, resourceHeaders);
    getIndex(res, serializedRoot, serializedRyvrsCollection, resourceHeaders);
    res.setStatus(HttpStatus.OK.value());

  }

  public void getRoot(final HttpServletResponse res, HttpServletRequest req) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serialiser.toJson(root, baos);
    String serializedRoot = baos.toString();
    res.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);
    JsonController.addLinks(root, res);
    getIndex(res, serializedRoot, serializedRoot, new HttpHeaders());
    res.setStatus(HttpStatus.OK.value());

  }

  private void getIndex(HttpServletResponse res, String root, String resource,
      HttpHeaders headers) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ClassPathResource index = new ClassPathResource("static/index.html");
    try {
      Mustache mustache = mustacheFactory.compile(new InputStreamReader(index.getInputStream()),
          "static/index.html", "<%", "%>");
      Map<String, String> scope = new HashMap<>();
      scope.put("root", root);
      Collection<String> rootLinkHeaders = res.getHeaders(HttpHeaders.LINK);
      HttpHeaders rootLinkHttpHeaders = new HttpHeaders();
      for (String header : rootLinkHeaders) {
        rootLinkHttpHeaders.add(HttpHeaders.LINK.toLowerCase(), header);
      }
      scope.put("root-headers", om.writeValueAsString(rootLinkHttpHeaders));

      scope.put("resource", resource);
      scope.put("resource-headers", om.writeValueAsString(headers));

      OutputStreamWriter writer = new OutputStreamWriter(baos);
      mustache.execute(writer, scope).flush();
      writer.flush();
      res.getOutputStream().write(baos.toByteArray());
      res.getOutputStream().flush();
      res.setStatus(org.apache.http.HttpStatus.SC_OK);
    } catch (IOException e) {
      throw new NotImplementedException(e);
    }
  }

  public ResponseEntity<?> getRyvr(HttpServletResponse res, HttpServletRequest req, String ryvrName,
      long page) throws URISyntaxException, IOException {

    // Ryvr ryvr = null;
    // try {
    // ryvr = ryvrsCollection.get(ryvrName);
    // } catch (NoSuchElementException e) {
    // // res.setStatus(org.apache.http.HttpStatus.SC_NOT_FOUND);
    // // return null;
    // throw new ResourceNotFoundException(req);
    // }
    //
    // if (ryvr == null) {
    // throw new ResourceNotFoundException(req);
    // }
    //
    // RyvrRoot root = (RyvrRoot) jsonController.getRoot(req).getBody();
    //
    // MockHttpServletResponse rvyrRes = new MockHttpServletResponse();
    // jsonController.getRyvr(rvyrRes, req, ryvrName, page);
    // HttpHeaders headers = new HttpHeaders();
    // for (Iterator<String> i = rvyrRes.getHeaderNames().iterator(); i.hasNext();) {
    // String headerName = i.next();
    // headers.put(headerName.toLowerCase(), rvyrRes.getHeaders(headerName));
    // }
    // return getIndex(root, rvyrRes.getContentAsString(), headers);
    throw new NotImplementedException("TODO");
  }

}
