package au.com.mountainpass.ryvr.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.RyvrSource;
import net.minidev.json.JSONArray;

public class RestRyvrSource extends RyvrSource {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private URI contextUri;
  private CloseableHttpClient httpClient;
  private HttpEntity body;
  URI currentUri;
  private CloseableHttpResponse response;
  int pageSize = -1;
  private int currentPageSize = -1;
  private int page = -1;
  long pagePosition;

  private boolean archivePageSet;

  private boolean archivePage;

  public RestRyvrSource(CloseableHttpClient httpClient, URI ryvrUri, HttpEntity body,
      CloseableHttpResponse response) {
    this.body = body;
    this.httpClient = httpClient;
    this.contextUri = ryvrUri;
    this.response = response;
    this.currentUri = contextUri;
  }

  private void followLink(String rel) {
    URI ryvrUri;
    try {
      ryvrUri = contextUri.resolve(getLink(rel));
      followUri(ryvrUri);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  void followNextLink() {
    URI ryvrUri;
    try {
      ryvrUri = contextUri.resolve(getNextLink());
      followUri(ryvrUri);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new NotImplementedException(e);
    }

  }

  void followUri(final URI ryvrUri) throws ClientProtocolException, IOException {
    HttpGet httpget = buildHttpGet(ryvrUri);
    if (response != null) {
      response.close();
    }
    response = httpClient.execute(httpget);
    body = response.getEntity();
    // LOGGER.info("Status: {}", response.getStatusLine());
    currentUri = ryvrUri;
    pageSize = -1;
    currentPageSize = -1;
    page = -1;
    nextLinkSet = false;
    nextLink = null;
    parsedBody = null;
    archivePageSet = false;
    // count = -1;
  }

  private HttpGet buildHttpGet(final URI ryvrUri) {
    final HttpGet httpget = buildInitialHttpGet();
    httpget.reset();
    httpget.setURI(ryvrUri);
    return httpget;
  }

  private HttpGet buildInitialHttpGet() {
    final HttpGet httpget = new HttpGet();
    httpget.setHeader("Accept", "application/json");
    return httpget;
  }

  private boolean nextLinkSet = false;
  private String nextLink = null;

  private Object parsedBody = null;
  private long count = -1L;

  private Object getBodyDocument() {
    if (parsedBody == null) {
      try {
        parsedBody = Configuration.defaultConfiguration().jsonProvider().parse(body.getContent(),
            StandardCharsets.UTF_8.name());
      } catch (InvalidJsonException | UnsupportedOperationException | IOException e) {
        throw new RuntimeException(e);
      }
    }
    return parsedBody;
  }

  // @Override
  // public Iterator<Record> iterator() {
  // return new RyvrIterator();
  // }

  // @Override
  // public ListIterator<Record> listIterator(int position) {
  // return new RyvrIterator(position);
  // }

  public String getNextLink() {
    if (!nextLinkSet) {
      nextLink = LinkHeader.extractUriByRel(response.getHeaders(HttpHeaders.LINK), "next");
      nextLinkSet = true;
    }
    return nextLink;
  }

  public String getLink(String rel) {
    return LinkHeader.extractUriByRel(response.getHeaders(HttpHeaders.LINK), rel);
  }

  public int getContentLength() {
    return Integer.parseInt(response.getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue());
  }

  // @Override
  // public long longSize() {
  // if (count == -1L) {
  // // LOGGER.info("getting current page to get size");
  // followLink("current");
  // // LOGGER.info("got current page");
  // Integer intCount = JsonPath.read(getBodyDocument(), "$.count");
  // count = intCount.intValue();
  // // LOGGER.info("size: {}", count);
  // }
  // return count;
  // }

  public int getUnderlyingPageSize() {
    if (pageSize < 0) {
      pageSize = Integer.parseInt(response.getFirstHeader("Page-Size").getValue());
    }
    return pageSize;
  }

  /**
   * @return the currentUri
   */
  public URI getCurrentUri() {
    return currentUri;
  }

  Record record = new Record() {

    @Override
    public int size() {
      return JsonPath.read(getBodyDocument(), "$.columns.length()");
    }

    @Override
    public Field getField(int fieldIndex) {
      final Field field = new Field() {

        private int fieldIndex;

        @Override
        public Object getValue() {
          return JsonPath.read(getBodyDocument(),
              "$.rows[" + pagePosition + "][" + fieldIndex + "]");
        }

        @Override
        public String getName() {
          return JsonPath.read(getBodyDocument(), "$.columns[" + fieldIndex + "]");
        }

        @Override
        public void setFieldIndex(int fieldIndex) {
          this.fieldIndex = fieldIndex;
        }
      };
      field.setFieldIndex(fieldIndex);
      return field;
    }
  };

  // @Override
  // public Record get(int index) {
  // int page = (index / getUnderlyingPageSize()) + 1;
  // pagePosition = index % getUnderlyingPageSize();
  // int currentPage = getPageNo();
  // if (currentPage != page) {
  // String currentFile = currentUri.getPath();
  // String currentQuery = currentUri.getQuery();
  // String newQuery = "page=" + page;
  // try {
  // // LOGGER.info("getting new page: {} for index {}", page, index);
  // followUri(currentUri.resolve(currentFile + "?" + newQuery));
  // } catch (IOException e) {
  // throw new RuntimeException(e);
  // }
  // }
  // return record;
  // }

  int getPageNo() {
    if (page < 0) {
      page = Integer.parseInt(response.getFirstHeader("Page").getValue());
    }
    return page;

  }

  @Override
  public String[] getFieldNames() {
    JSONArray array = JsonPath.read(getBodyDocument(), "$.columns");
    return array.toArray(new String[] {});
  }

  @Override
  public void refresh() {
    // LOGGER.info("resetting size");
    // count = -1L;
    followLink("self");
  }

  @Override
  public Iterator<Record> iterator() {
    return new RestRyvrSourceIterator(this);
  }

  @Override
  public Iterator<Record> iterator(long position) {
    return new RestRyvrSourceIterator(this, position);
  }

  public int getUnderlyingCurrentPageSize() {
    if (currentPageSize < 0) {
      currentPageSize = Integer.parseInt(response.getFirstHeader("Current-Page-Size").getValue());
    }
    return currentPageSize;
  }

  @Override
  public long getRecordsRemaining(long fromPosition) {
    throw new NotImplementedException("TODO");
  }

  public boolean isArchivePage() {
    if (!archivePageSet) {
      archivePage = Boolean.parseBoolean(response.getFirstHeader("Archive-Page").getValue());
      archivePageSet = true;
    }
    return archivePage;
  }

  @Override
  public boolean isLoaded(long page) {
    return response != null;
  }

}
