package au.com.mountainpass.ryvr.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.traverson.Traverson;
import net.minidev.json.JSONArray;

public class RestRyvr extends Ryvr {

  private Traverson traverson;
  private URI contextUri;
  private RestTemplate restTemplate;
  private CloseableHttpAsyncClient httpAsyncClient;
  private CloseableHttpClient httpClient;
  private HttpEntity body;
  private URI currentUri;
  private CloseableHttpResponse response;

  public RestRyvr(String title, CloseableHttpClient httpClient,
      CloseableHttpAsyncClient httpAsyncClient, Traverson traverson, URI ryvrUri, HttpEntity body,
      RestTemplate restTemplate, CloseableHttpResponse response) {
    super(title);
    this.body = body;
    this.httpClient = httpClient;
    this.httpAsyncClient = httpAsyncClient;
    this.traverson = traverson;
    this.contextUri = ryvrUri;
    this.restTemplate = restTemplate;
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
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new NotImplementedException();
    }

  }

  private void followNextLink() {
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

  private void followUri(final URI ryvrUri) throws ClientProtocolException, IOException {
    HttpGet httpget = buildHttpGet(ryvrUri);
    if (response != null) {
      response.close();
    }
    response = httpClient.execute(httpget);
    if (response.getStatusLine().getStatusCode() == HttpStatus.SEE_OTHER.value()) {
      httpget = buildHttpGet(URI.create(response.getFirstHeader(HttpHeaders.LOCATION).getValue()));
      response.close();
      response = httpClient.execute(httpget);
    }
    body = response.getEntity();
    currentUri = ryvrUri;
    pageSize = -1;
    nextLinkSet = false;
    nextLink = null;
    parsedBody = null;
    count = -1;
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

  private class RyvrIterator implements Iterator<Record> {
    long pagePosition = -1;
    long currentPage = -1;

    @Override
    public boolean hasNext() {
      if (currentPage < 0) {
        // first call to hasNext, so load the first page and check if we have records;
        followLink("first");
        currentPage = 0;
        return getPageSize() != 0;
      } else if (getNextLink() != null) {
        // if there is a next link, then there are definitely next records
        return true;
      } else {

        // otherwise we are on the most recent page (AKA the current page)
        // so check if there are rows after the row we are pointing to at
        // the moment.
        int recordsOnPage = getPageSize();
        if (pagePosition < recordsOnPage - 1) {
          return true;
        } else {
          // otehrwise, relaod and see if we have new events
          followLink("self");
          recordsOnPage = getPageSize();
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

        @Override
        public void setPosition(long pp) {
          pagePosition = (int) pp;
        }
      };
      ++pagePosition;
      if (pagePosition == getPageSize()) {
        followNextLink();
        this.pagePosition = 0;
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

  }

  @Override
  public Iterator<Record> iterator() {
    return new RyvrIterator();
  }

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

  @Override
  public long getCount() {
    if (count == -1L) {
      if (currentUri == null) {
        try {
          followUri(contextUri);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else if (!currentUri.getPath().equals(getLink("current"))
          && !currentUri.getPath().equals(getLink("last"))) {
        followLink("current");
      }
      Integer intCount = JsonPath.read(getBodyDocument(), "$.count");
      count = intCount;
    }
    return count;
  }

  private int pageSize = -1;

  @Override
  public int getPageSize() {
    if (pageSize < 0) {
      pageSize = Integer.parseInt(response.getFirstHeader("Page-Record-Count").getValue());
    }
    return pageSize;
  }

  /**
   * @return the currentUri
   */
  public URI getCurrentUri() {
    return currentUri;
  }

  @Override
  public String[] getFieldNames() {
    JSONArray array = JsonPath.read(getBodyDocument(), "$.columns");
    return array.toArray(new String[] {});
  }

}
