package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;
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
import au.com.mountainpass.ryvr.model.RestRyvrRootImpl;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrRoot;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.rest.RestRyvrSource;
import au.com.mountainpass.ryvr.testclient.model.RestRyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerImpl;
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
  public Ryvr getRyvr(String name) throws MalformedURLException {
    return getRyvrsCollection().followRyvrLink(name);
  }

  @Override
  public void after(Scenario scenario) {
  }

  @Override
  public void before(Scenario scenario) {
  }

  @Override
  public Ryvr getRyvrDirect(String name, int page) throws Throwable {
    // instead of following the links, we are going to just construct the
    // URL and hit it directly, to ensure the correct 404 is returned
    URL contextUrl = getRyvrsCollection().getContextUrl();
    try {
      URI ryvrUri = contextUrl.toURI().resolve("/ryvrs/" + name + "?page=" + page);
      final HttpGet httpget = new HttpGet(ryvrUri);
      httpget.reset();
      httpget.addHeader("Accept", "application/json");
      CloseableHttpResponse response = httpClient.execute(httpget);
      switch (response.getStatusLine().getStatusCode()) {
      case org.apache.http.HttpStatus.SC_NOT_FOUND:
        throw new NoSuchElementException("No value present");
      case org.apache.http.HttpStatus.SC_SEE_OTHER:
        httpget.reset();
        httpget.setURI(URI.create(response.getFirstHeader(HttpHeaders.LOCATION).getValue()));
        response = httpClient.execute(httpget);
        break;
      case org.apache.http.HttpStatus.SC_OK:
        break;
      default:
        throw new NotImplementedException(
            "Unexpected status code: " + response.getStatusLine().getStatusCode());
      }

      return new Ryvr(name, 10,
          new RestRyvrSource(httpClient, ryvrUri, response.getEntity(), response));
    } catch (URISyntaxException | IOException e) {
      throw new NotImplementedException(e);
    }
  }

  @Override
  public RyvrsCollectionResponse getRyvrsCollectionDirect() throws Throwable {
    try {
      URI ryvrsUri = config.getBaseUri().resolve("/ryvrs");
      RyvrsCollection ryvrsCollection = restTemplate.getForEntity(ryvrsUri, RyvrsCollection.class)
          .getBody();
      return new RestRyvrsCollectionResponse(httpClient, httpAsyncClient, ryvrsUri.toURL(),
          ryvrsCollection);
    } catch (MalformedURLException e) {
      throw new NotImplementedException(e);
    }
  }

}
