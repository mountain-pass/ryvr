package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrResponse extends JavaRyvrResponse {

  private Traverson traverson;
  private URL contextUrl;
  private RestTemplate restTemplate;
  private final HttpHeaders httpHeaders;
  private CloseableHttpAsyncClient httpAsyncClient;
  private CloseableHttpClient httpClient;

  public RestRyvrResponse(CloseableHttpClient httpClient, CloseableHttpAsyncClient httpAsyncClient,
      Traverson traverson, URL contextUrl, Ryvr ryvr, RestTemplate restTemplate,
      HttpHeaders httpHeaders) {
    super(ryvr);
    this.httpClient = httpClient;
    this.httpAsyncClient = httpAsyncClient;
    this.traverson = traverson;
    this.contextUrl = contextUrl;
    this.restTemplate = restTemplate;
    this.httpHeaders = httpHeaders;
  }

  // credit: https://gist.github.com/eugenp/8269915
  private static String extractURIByRel(final List<String> list, final String rel) {
    String uriWithSpecifiedRel = null;
    String linkRelation = null;
    for (final String link : list) {
      final int positionOfSeparator = link.indexOf(';');
      linkRelation = link.substring(positionOfSeparator + 1, link.length()).trim();
      if (extractTypeOfRelation(linkRelation).equals(rel)) {
        uriWithSpecifiedRel = link.substring(1, positionOfSeparator - 1);
        break;
      }
    }

    return uriWithSpecifiedRel;
  }

  private static Object extractTypeOfRelation(final String linkRelation) {
    final int positionOfEquals = linkRelation.indexOf('=');
    return linkRelation.substring(positionOfEquals + 2, linkRelation.length() - 1).trim();
  }

  @Override
  public RyvrResponse followLink(String rel) {
    URI ryvrUri;
    try {
      ryvrUri = contextUrl.toURI().resolve(extractURIByRel(httpHeaders.get(HttpHeaders.LINK), rel));
    } catch (URISyntaxException ex) {
      throw new NotImplementedException(ex);
    }

    final HttpGet httpget = new HttpGet(ryvrUri);
    httpget.addHeader("Accept", "application/hal+json, application/json");
    CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<HttpResponse>();

    CloseableHttpResponse response;
    try {
      response = httpClient.execute(httpget);
    } catch (IOException ex) {
      throw new NotImplementedException(ex);
    }
    HttpHeaders headers = new HttpHeaders();
    for (Header header : response.getAllHeaders()) {
      headers.add(header.getName(), header.getValue());
    }
    long length = headers.getContentLength();
    if (length < 0L) {
      length = response.getEntity().getContentLength();
    }
    if (length < 0L) {
      try {
        length = response.getEntity().getContent().available();
      } catch (Exception ex) {
        throw new NotImplementedException(ex);
      }
    }

    receivedBytes.observe(length);
    try {
      return new RestRyvrResponse(httpClient, httpAsyncClient, traverson, ryvrUri.toURL(), null,
          restTemplate, headers);
    } catch (Exception ex) {
      throw new NotImplementedException(ex);
    }
  }

  @Override
  public void assertFromCache() {
    assertThat(httpHeaders.get("X-Cache"), contains("HIT"));
  }

  @Override
  public void assertNotFromCache() {
    assertThat(httpHeaders.get("X-Cache"), not(contains("HIT")));
  }

  @Override
  public void assertHasLinks(List<String> links) {
    links.forEach(rel -> {
      String ryvrUri = extractURIByRel(httpHeaders.get(HttpHeaders.LINK), rel);
      assertThat(ryvrUri, notNullValue());
    });
  }

  @Override
  public void assertDoesntHaveLinks(List<String> links) {
    links.forEach(rel -> {
      String ryvrUri = extractURIByRel(httpHeaders.get(HttpHeaders.LINK), rel);
      assertThat(ryvrUri, nullValue());
    });
  }

  @Override
  public boolean hasLink(String rel) {
    return extractURIByRel(httpHeaders.get(HttpHeaders.LINK), rel) != null;
  }
}
