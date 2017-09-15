package au.com.mountainpass.ryvr.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
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
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.Ryvr;
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

  public void getRyvr(HttpServletResponse res, HttpServletRequest req, String ryvrName, long page)
      throws URISyntaxException, IOException {

    Ryvr ryvr = ryvrsCollection.get(ryvrName);
    if (ryvr == null) {
      throw new ResourceNotFoundException(req);
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serialiser.toJson(root, baos);
    String serializedRoot = baos.toString();
    res.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);
    JsonController.addLinks(root, res);

    boolean isLastPage = false;
    long pageSize = ryvr.getPageSize();
    long pageRecordCount = 0;

    boolean isLoaded = ryvr.getSource().isLoaded(page);
    long pageEndPosition = JsonController.getPageEndPosition(page, pageSize);
    Iterator<Record> lastOnPageIterator = ryvr.getSource().iterator(pageEndPosition + 1L);

    isLastPage = !lastOnPageIterator.hasNext();
    if (isLastPage && isLoaded) {
      // since we are on the last page, and we already had the page loaded, refresh to see if
      // there are new records
      // and then check if we are on the lastPage again.
      ryvr.getSource().refresh();
      lastOnPageIterator = ryvr.getSource().iterator(pageEndPosition + 1L);
      isLastPage = !lastOnPageIterator.hasNext();
    }

    res.addHeader(HttpHeaders.VARY, String.join(",", HttpHeaders.AUTHORIZATION, HttpHeaders.ACCEPT,
        HttpHeaders.ACCEPT_ENCODING, HttpHeaders.ACCEPT_LANGUAGE, HttpHeaders.ACCEPT_CHARSET));

    if (isLastPage) {
      res.addHeader(HttpHeaders.CACHE_CONTROL,
          CacheControl.maxAge(currentPageMaxAge, currentPageMaxAgeUnit).getHeaderValue());
      pageRecordCount = ryvr.getSource()
          .getRecordsRemaining(JsonController.getPageStartPosition(page, pageSize));
      res.addHeader(HttpHeaders.ETAG,
          "\"" + Long.toHexString(page) + "." + Long.toHexString(pageRecordCount) + "\"");
      res.addHeader("Current-Page-Size", Long.toString(pageRecordCount));
      res.addHeader("Archive-Page", "false");

    } else {
      res.addHeader(HttpHeaders.CACHE_CONTROL,
          CacheControl.maxAge(archivePageMaxAge, archivePageMaxAgeUnit).getHeaderValue());
      res.addHeader(HttpHeaders.ETAG, "\"" + Long.toHexString(page) + "\"");
      res.addHeader("Current-Page-Size", Long.toString(pageSize));
      res.addHeader("Archive-Page", "true");

    }
    res.addHeader("Page", Long.toString(page));
    res.addHeader("Page-Size", Long.toString(pageSize));

    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
    serialiser.toJson(ryvr, page, baos2);
    String serializedRyvr = baos2.toString();
    HttpHeaders resourceHeaders = new HttpHeaders();
    JsonController.addLinks(ryvr, page, isLastPage, resourceHeaders);
    getIndex(res, serializedRoot, serializedRyvr, resourceHeaders);
    res.setStatus(HttpStatus.OK.value());
  }

}
