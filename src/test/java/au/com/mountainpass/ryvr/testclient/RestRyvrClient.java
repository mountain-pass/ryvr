package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.RestRyvrCollectionImpl;
import au.com.mountainpass.ryvr.model.RestRyvrRootImpl;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrRoot;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.model.SwaggerImpl;
import au.com.mountainpass.ryvr.rest.RestRyvrSource;
import cucumber.api.Scenario;

public class RestRyvrClient implements RyvrTestClient {
  private static Logger LOGGER = LoggerFactory.getLogger(RestRyvrClient.class);

  @Autowired
  private RyvrConfiguration config;

  @Autowired
  private CloseableHttpAsyncClient httpAsyncClient;

  @Autowired
  private CloseableHttpClient httpClient;

  @Autowired
  private CookieStore cookies;

  @Override
  public SwaggerImpl getApiDocs() throws InterruptedException, ExecutionException,
      URISyntaxException, ClientProtocolException, IOException {
    RyvrRoot root = getRoot();
    return root.getApiDocs();
  }

  @Override
  public RyvrRoot getRoot() throws ClientProtocolException, IOException {
    URI baseUri = config.getBaseUri();
    final HttpGet httpget = new HttpGet();
    httpget.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
    httpget.reset();
    httpget.setURI(baseUri);
    try (CloseableHttpResponse response = httpClient.execute(httpget)) {
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        return new RyvrRoot("ryvr", new RestRyvrRootImpl(httpClient, cookies, response, baseUri));
      } else {
        throw new NotImplementedException("TODO: handle " + response.getStatusLine().toString());
      }
    }
  }

  @Override
  public RyvrsCollection getRyvrsCollection() throws ClientProtocolException, IOException {
    return getRoot().getRyvrsCollection();
  }

  @Override
  public Ryvr getRyvr(String name) throws ClientProtocolException, IOException {
    return getRyvrsCollection().get(name);
  }

  @Override
  public void after(Scenario scenario) {
  }

  @Override
  public void before(Scenario scenario) {
  }

  @Override
  public Ryvr getRyvrDirect(String name, int page) throws ClientProtocolException, IOException {
    // instead of following the links, we are going to just construct the
    // URL and hit it directly, to ensure the correct 404 is returned
    URI ryvrUri = config.getBaseUri().resolve("/ryvrs/" + name + "?page=" + page);
    final HttpGet httpget = new HttpGet(ryvrUri);
    httpget.reset();
    httpget.addHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
    CloseableHttpResponse response = httpClient.execute(httpget);
    switch (response.getStatusLine().getStatusCode()) {
    case org.apache.http.HttpStatus.SC_NOT_FOUND:
      return null;
    case org.apache.http.HttpStatus.SC_SEE_OTHER:
      httpget.reset();
      httpget.setURI(URI.create(response.getFirstHeader(HttpHeaders.LOCATION).getValue()));
      response = httpClient.execute(httpget);
      break;
    case org.apache.http.HttpStatus.SC_OK:
      break;
    default:
      throw new NotImplementedException(
          "TODO - handle: " + response.getStatusLine().getStatusCode());
    }

    return new Ryvr(name, 10,
        new RestRyvrSource(httpClient, ryvrUri, response.getEntity(), response));

  }

  @Override
  public RyvrsCollection getRyvrsCollectionDirect() throws Throwable {
    URI ryvrsUri = config.getBaseUri().resolve("/ryvrs");
    HttpGet request = new HttpGet(ryvrsUri);
    request.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
    CloseableHttpResponse response = httpClient.execute(request);
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      return new RyvrsCollection(
          new RestRyvrCollectionImpl(httpClient, cookies, response, config.getBaseUri()));
    } else {
      throw new NotImplementedException("TODO: handle " + response.getStatusLine().toString());
    }

  }

}
