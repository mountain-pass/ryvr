package au.com.mountainpass.ryvr.controllers;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.stereotype.Component;

import au.com.mountainpass.ryvr.model.Link;
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
    addLinks(responseBuilder, ryvrsCollection.getLinks());

    return responseBuilder.body(ryvrsCollection);
  }

  public ResponseEntity<?> getRoot(HttpServletRequest req) {
    Root root = new Root(applicationName);
    return ResponseEntity.ok().contentType(APPLICATION_HAL_JSON_TYPE).body(root);
  }

  public ResponseEntity<?> getRyvr(HttpServletRequest req, String ryvrName, long page)
      throws URISyntaxException {

    Ryvr ryvr = ryvrsCollection.getRyvr(ryvrName);
    if (ryvr == null) {
      return ResponseEntity.notFound().build();
    }
    boolean cachable = ryvr.refreshPage(page);

    BodyBuilder responseBuilder = ResponseEntity.ok().contentType(APPLICATION_HAL_JSON_TYPE);
    if (cachable) {
      responseBuilder.cacheControl(CacheControl.maxAge(archivePageMaxAge, archivePageMaxAgeUnit));
    } else {
      responseBuilder.cacheControl(CacheControl.maxAge(currentPageMaxAge, currentPageMaxAgeUnit));
    }
    responseBuilder.eTag(ryvr.getEtag());

    addLinks(responseBuilder, ryvr.getLinks());
    return responseBuilder.body(ryvr);
  }

  public void addLinks(BodyBuilder responseBuilder, Map<String, Link[]> links) {
    for (Entry<String, Link[]> entry : links.entrySet()) {
      for (Link link : entry.getValue()) {
        String headerValue = "<" + link.getHref() + ">; rel=\"" + entry.getKey() + "\"";
        if (link.getTitle() != null) {
          headerValue += "; title=\"" + link.getTitle() + "\"";
        }
        responseBuilder.header(HttpHeaders.LINK, headerValue);
      }
    }
  }
}
