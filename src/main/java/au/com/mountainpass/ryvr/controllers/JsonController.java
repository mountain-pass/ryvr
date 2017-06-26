package au.com.mountainpass.ryvr.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.io.RyvrSerialiser;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

@Component()
public class JsonController {

  public static final MediaType APPLICATION_HAL_JSON_TYPE = new MediaType("application",
      "hal+json");

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

  @Autowired
  private RyvrsCollection ryvrsCollection;

  @Autowired
  private RyvrConfiguration config;

  @Autowired
  private RyvrSerialiser serialiser;

  public ResponseEntity<?> getApiDocs(HttpServletRequest req, String group) {
    ClassPathResource index = new ClassPathResource("static/swagger.json");

    StringWriter writer = new StringWriter();
    try {
      IOUtils.copy(index.getInputStream(), writer, "UTF-8");
      return ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .body(writer.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public ResponseEntity<?> getRyvrsCollection(HttpServletRequest req) {
    BodyBuilder responseBuilder = ResponseEntity.ok().contentType(APPLICATION_HAL_JSON_TYPE);
    // addLinks(responseBuilder, ryvrsCollection.getLinks());

    return responseBuilder.body(ryvrsCollection);
  }

  public ResponseEntity<?> getRoot(HttpServletRequest req) {
    Root root = new Root(applicationName);
    return ResponseEntity.ok().contentType(APPLICATION_HAL_JSON_TYPE).body(root);
  }

  public ResponseEntity<StreamingResponseBody> getRyvr(HttpServletRequest req, String ryvrName,
      long page) throws URISyntaxException {

    Ryvr ryvr = ryvrsCollection.getRyvr(ryvrName);
    if (ryvr == null) {
      return ResponseEntity.notFound().build();
    }
    Iterator<Record> iterator = ryvr.iterator((page - 1) * ryvr.getPageSize());

    boolean cachable = false;

    BodyBuilder responseBuilder = ResponseEntity.ok().contentType(APPLICATION_HAL_JSON_TYPE);
    if (cachable) {
      responseBuilder.cacheControl(CacheControl.maxAge(archivePageMaxAge, archivePageMaxAgeUnit));
    } else {
      responseBuilder.cacheControl(CacheControl.maxAge(currentPageMaxAge, currentPageMaxAgeUnit));
    }
    // responseBuilder.eTag(ryvr.getEtag());

    addLinks(ryvr, page, responseBuilder);
    responseBuilder.header("Page-Record-Count", Integer.toString(ryvr.getCurrentPageSize(page)));
    return responseBuilder.body(new StreamingResponseBody() {
      @Override
      public void writeTo(OutputStream outputStream) throws IOException {
        serialiser.toJson(ryvr, page, outputStream);
      }
    });
  }

  public void addLinks(Ryvr ryvr, long page, BodyBuilder responseBuilder) {

    String base = "/ryvrs/" + ryvr.getTitle();
    addLink("current", base, responseBuilder);
    addLink("self", base + "?page=" + page, responseBuilder);
    addLink("first", base + "?page=1", responseBuilder);
    if (page > 1) {
      addLink("prev", base + "?page=" + (page - 1L), responseBuilder);
    }
    if (page < ryvr.getPages()) {
      addLink("next", base + "?page=" + (page + 1L), responseBuilder);
      addLink("last", base, responseBuilder);
    } else {
      addLink("last", base + "?page=" + (ryvr.getPages()), responseBuilder);
    }

  }

  private void addLink(String rel, String href, BodyBuilder responseBuilder) {
    String headerValue = "<" + href + ">; rel=\"" + rel + "\"";
    responseBuilder.header(HttpHeaders.LINK, headerValue);
  }

  public ResponseEntity<StreamingResponseBody> getRyvr(HttpServletRequest req, String ryvrName) {
    Ryvr ryvr = ryvrsCollection.getRyvr(ryvrName);
    if (ryvr == null) {
      return ResponseEntity.notFound().build();
    }
    String lastHref = "/ryvrs/" + ryvr.getTitle() + "?page=" + (ryvr.getPages());
    return ResponseEntity.status(HttpStatus.SEE_OTHER)
        .location(config.getBaseUri().resolve(lastHref)).build();
  }
}
