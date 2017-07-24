package au.com.mountainpass.ryvr.controllers;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    Ryvr ryvr = ryvrsCollection.getRyvr(ryvrName);
    if (ryvr == null) {
      res.setStatus(HttpStatus.NOT_FOUND.value());
      return;
    }
    boolean isLastPage = false;
    long pageSize = ryvr.getPageSize();
    if (page == -1L) {
      isLastPage = true;
      long count = ryvr.getSource().getRecordsRemaining(0L);
      page = (count / pageSize) + 1L;
    } else {
      // get the iterator for the last possible element on this page
      // this might be beyond the end of the ryvr
      boolean isLoaded = ryvr.getSource().isLoaded(page);

      long pageEndPosition = getPageEndPosition(page, pageSize);
      Iterator<Record> lastOnPageIterator = ryvr.getSource().iterator(pageEndPosition);

      isLastPage = !lastOnPageIterator.hasNext();
      if (isLastPage && isLoaded) {
        // since we are on the last page, and we already had the page loaded, refresh to see if
        // there are new records
        // and then check if we are on the lastPage again.
        ryvr.getSource().refresh();
        lastOnPageIterator = ryvr.getSource().iterator(pageEndPosition);
        isLastPage = !lastOnPageIterator.hasNext();
      }
    }
    // BodyBuilder responseBuilder = ResponseEntity.ok().contentType(APPLICATION_HAL_JSON_TYPE);
    res.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    if (isLastPage) {
      res.addHeader(HttpHeaders.CACHE_CONTROL,
          CacheControl.maxAge(currentPageMaxAge, currentPageMaxAgeUnit).getHeaderValue());
      // responseBuilder.cacheControl(CacheControl.maxAge(currentPageMaxAge,
      // currentPageMaxAgeUnit));
      long pageRecordCount = ryvr.getSource()
          .getRecordsRemaining(getPageStartPosition(page, pageSize));
      res.addHeader(HttpHeaders.ETAG,
          "\"" + Long.toHexString(page) + "." + Long.toHexString(pageRecordCount) + "\"");
      res.addHeader("Current-Page-Size", Long.toString(pageRecordCount));
      res.addHeader("Archive-Page", "false");
      // responseBuilder.eTag(Long.toHexString(page) + "." + Long.toHexString(pageRecordCount));
      // responseBuilder.header("Current-Page-Size", Long.toString(pageRecordCount));
    } else {
      res.addHeader(HttpHeaders.CACHE_CONTROL,
          CacheControl.maxAge(archivePageMaxAge, archivePageMaxAgeUnit).getHeaderValue());
      res.addHeader(HttpHeaders.ETAG, "\"" + Long.toHexString(page) + "\"");
      res.addHeader("Current-Page-Size", Long.toString(pageSize));
      res.addHeader("Archive-Page", "true");

      // responseBuilder.cacheControl(CacheControl.maxAge(archivePageMaxAge,
      // archivePageMaxAgeUnit));
      // responseBuilder.eTag(Long.toHexString(page));
      // responseBuilder.header("Current-Page-Size", Long.toString(pageSize));
    }
    res.addHeader("Page", Long.toString(page));
    res.addHeader("Page-Size", Long.toString(pageSize));

    // responseBuilder.header("Page", Long.toString(page));
    // responseBuilder.header("Page-Size", Integer.toString(pageSize));

    addLinks(ryvr, page, isLastPage, res);
    // responseBuilder.header("Current-Page-Size", Integer.toString(ryvr.getCurrentPageSize(page)));
    serialiser.toJson(ryvr, page, res.getOutputStream());
    res.setStatus(HttpStatus.OK.value());

    // return responseBuilder.body(new StreamingResponseBody() {
    // @Override
    // public void writeTo(OutputStream outputStream) throws IOException {
    // serialiser.toJson(ryvr, page, outputStream);
    // }
    // });
  }

  private long getPageEndPosition(long page, long pageSize) {
    return getPageStartPosition(page + 1L, pageSize) - 1L;
  }

  private long getPageStartPosition(long page, long pageSize) {
    return (page - 1L) * pageSize;
  }

  public void addLinks(Ryvr ryvr, long page, boolean isLastPage, HttpServletResponse res) {

    String base = "/ryvrs/" + ryvr.getTitle();
    addLink("current", base, res);
    if (page > 0L) {
      addLink("self", base + "?page=" + page, res);
    } else {
      addLink("self", base + "?page=" + page, res);
    }
    addLink("first", base + "?page=1", res);
    if (page > 1L) {
      addLink("prev", base + "?page=" + (page - 1L), res);
    }
    if (!isLastPage) {
      addLink("next", base + "?page=" + (page + 1L), res);
      addLink("last", base, res);
    } else {
      addLink("last", base + "?page=" + page, res);
    }
    String headerValue = "<" + base + "?page={page}" + ">; rel=\"" + RELS_PAGE
        + "\"; var-base=\"https://mountain-pass.github.io/ryvr/vars/\"";
    res.addHeader("Link-Template", headerValue);
  }

  private void addLink(String rel, String href, HttpServletResponse res) {
    String headerValue = "<" + href + ">; rel=\"" + rel + "\"";
    res.addHeader(HttpHeaders.LINK, headerValue);
  }

  public void getRyvr(HttpServletResponse res, HttpServletRequest req, String ryvrName)
      throws URISyntaxException, IOException {
    // we can't redirect to the last page, becasue by the time we get that page, it might no longer
    // be
    // the last, in which case we can't get the count of records, which is what we need for
    // AbstractList::size()
    // hmm maybe we don't use abstract list...
    getRyvr(res, req, ryvrName, -1L);
    // Ryvr ryvr = ryvrsCollection.getRyvr(ryvrName);
    // if (ryvr == null) {
    // return ResponseEntity.notFound().build();
    // }
    // String lastHref = "/ryvrs/" + ryvr.getTitle() + "?page=" + (ryvr.getPages());
    // return ResponseEntity.status(HttpStatus.SEE_OTHER)
    // .location(config.getBaseUri().resolve(lastHref)).build();
  }
}
