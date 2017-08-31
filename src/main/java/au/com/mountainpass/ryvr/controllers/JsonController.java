package au.com.mountainpass.ryvr.controllers;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.io.RyvrSerialiser;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

@Component()
public class JsonController {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  public static final MediaType APPLICATION_HAL_JSON_TYPE = new MediaType("application",
      "hal+json");

  public static final String RELS_PAGE = "https://mountain-pass.github.io/ryvr/rels/page";

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

  public void getRyvr(HttpServletResponse res, HttpServletRequest req, String ryvrName, long page)
      throws URISyntaxException, IOException {
    // hmmm... we want to avoid having a size operation on the rvyr, because that can
    // be very slow, so the only way we know if we are on an archive page is if
    // the iterator we use for outputting hasNext(), or if we get another iterator and
    // advance end and then check if it hasNext(). Going with that latter option.
    Ryvr ryvr = null;
    try {
      ryvr = ryvrsCollection.getRyvr(ryvrName);
    } catch (NoSuchElementException e) {
      res.setStatus(HttpStatus.NOT_FOUND.value());
      return;
    }
    if (ryvr == null) {
      res.setStatus(HttpStatus.NOT_FOUND.value());
      return;
    }
    boolean isLastPage = false;
    long pageSize = ryvr.getPageSize();
    long pageRecordCount = 0;

    boolean isLoaded = ryvr.getSource().isLoaded(page);
    // LOGGER.info("isLoaded: {}", isLoaded);
    long pageEndPosition = getPageEndPosition(page, pageSize);
    Iterator<Record> lastOnPageIterator = ryvr.getSource().iterator(pageEndPosition + 1L);

    isLastPage = !lastOnPageIterator.hasNext();
    // LOGGER.info("isLastPage: {}", isLastPage);
    if (isLastPage && isLoaded) {
      // since we are on the last page, and we already had the page loaded, refresh to see if
      // there are new records
      // and then check if we are on the lastPage again.
      LOGGER.debug("refreshing ryvr {}...", ryvrName);
      ryvr.getSource().refresh();
      lastOnPageIterator = ryvr.getSource().iterator(pageEndPosition + 1L);
      isLastPage = !lastOnPageIterator.hasNext();
    }
    // }
    res.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    res.addHeader(HttpHeaders.VARY, String.join(",", HttpHeaders.ACCEPT,
        HttpHeaders.ACCEPT_ENCODING, HttpHeaders.ACCEPT_LANGUAGE, HttpHeaders.ACCEPT_CHARSET));

    if (isLastPage) {
      res.addHeader(HttpHeaders.CACHE_CONTROL,
          CacheControl.maxAge(currentPageMaxAge, currentPageMaxAgeUnit).getHeaderValue());
      pageRecordCount = ryvr.getSource().getRecordsRemaining(getPageStartPosition(page, pageSize));
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

    addLinks(ryvr, page, isLastPage, res);
    serialiser.toJson(ryvr, page, res.getOutputStream());
    res.setStatus(HttpStatus.OK.value());

  }

  private long getPageEndPosition(long page, long pageSize) {
    return getPageStartPosition(page + 1L, pageSize) - 1L;
  }

  private long getPageStartPosition(long page, long pageSize) {
    return (page - 1L) * pageSize;
  }

  public void addLinks(Ryvr ryvr, long page, boolean isLastPage, HttpServletResponse res) {

    String base = "/ryvrs/" + ryvr.getTitle();
    addLink("self", base + "?page=" + page, res);
    addLink("first", base + "?page=1", res);
    if (page > 1L) {
      addLink("prev", base + "?page=" + (page - 1L), res);
    }
    if (!isLastPage) {
      addLink("next", base + "?page=" + (page + 1L), res);
    }
    String headerValue = "<" + base + "?page={page}" + ">; rel=\"" + RELS_PAGE
        + "\"; var-base=\"https://mountain-pass.github.io/ryvr/vars/\"";
    res.addHeader("Link-Template", headerValue);
  }

  private void addLink(String rel, String href, HttpServletResponse res) {
    String headerValue = "<" + href + ">; rel=\"" + rel + "\"";
    res.addHeader(HttpHeaders.LINK, headerValue);
  }
}
