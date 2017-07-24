package au.com.mountainpass.ryvr.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.Header;
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

  // credit: https://gist.github.com/eugenp/8269915
  private static String extractURIByRel(final Header[] headers, final String rel) {
    String uriWithSpecifiedRel = null;
    String linkRelation = null;
    for (final Header header : headers) {
      String headerValue = header.getValue();
      final int positionOfSeparator = headerValue.indexOf(';');
      linkRelation = headerValue.substring(positionOfSeparator + 1, headerValue.length()).trim();
      if (extractTypeOfRelation(linkRelation).equals(rel)) {
        uriWithSpecifiedRel = headerValue.substring(1, positionOfSeparator - 1);
        break;
      }
    }

    return uriWithSpecifiedRel;
  }

  private static String extractTypeOfRelation(final String linkRelation) {
    final int positionOfEquals = linkRelation.indexOf('=');
    return linkRelation.substring(positionOfEquals + 2, linkRelation.length() - 1).trim();
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
      throw new NotImplementedException();
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

  private class RyvrIterator implements ListIterator<Record> {
    long pagePosition = -1;
    long currentPage = -1;

    public RyvrIterator() {

    }

    public RyvrIterator(long position) {
      currentPage = position / getUnderlyingPageSize();
      pagePosition = position % getUnderlyingPageSize();

    }

    @Override
    public boolean hasNext() {
      if (currentPage < 0) {
        // first call to hasNext, so load the first page and check if we have records;
        followLink("first");
        currentPage = 0;
        return getUnderlyingPageSize() != 0;
      } else if (getNextLink() != null) {
        // if there is a next link, then there are definitely next records
        return true;
      } else {

        // otherwise we are on the most recent page (AKA the current page)
        // so check if there are rows after the row we are pointing to at
        // the moment.
        int recordsOnPage = getUnderlyingPageSize();
        if (pagePosition < recordsOnPage - 1) {
          return true;
        } else {
          // otehrwise, relaod and see if we have new events
          followLink("self");
          recordsOnPage = getUnderlyingPageSize();
        }
        return pagePosition < recordsOnPage - 1;
      }
    }

    @Override
    public Record next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      final Record rval = new Record() {

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
      ++pagePosition;
      if (pagePosition == getUnderlyingPageSize()) {
        followNextLink();
        this.pagePosition = 0;
        ++this.currentPage;
      }
      return rval;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super Record> consumer) {
      if (!hasNext()) {
        return;
      }
      while (hasNext()) {
        consumer.accept(next());
      }
    }

    @Override
    public boolean hasPrevious() {
      throw new NotImplementedException("TODO");
    }

    @Override
    public Record previous() {
      throw new NotImplementedException("TODO");
    }

    @Override
    public int nextIndex() {
      throw new NotImplementedException("TODO");
    }

    @Override
    public int previousIndex() {
      throw new NotImplementedException("TODO");
    }

    @Override
    public void set(Record e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(Record e) {
      throw new UnsupportedOperationException();
    }

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
      nextLink = extractURIByRel(response.getHeaders(HttpHeaders.LINK), "next");
      nextLinkSet = true;
    }
    return nextLink;
  }

  public String getLink(String rel) {
    return extractURIByRel(response.getHeaders(HttpHeaders.LINK), rel);
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

  public long getUnderlyingCurrentPageSize() {
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
