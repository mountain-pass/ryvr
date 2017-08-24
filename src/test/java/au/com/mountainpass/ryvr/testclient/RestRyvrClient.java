package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.rest.RestRyvrSource;
import au.com.mountainpass.ryvr.testclient.model.JavaSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RestRootResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;
import io.swagger.parser.SwaggerParser;

public class RestRyvrClient implements RyvrTestClient {
  private Logger logger = LoggerFactory.getLogger(RestRyvrClient.class);

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private RyvrConfiguration config;

  private SwaggerParser swaggerParser = new SwaggerParser();

  @Autowired
  private CloseableHttpAsyncClient httpAsyncClient;

  @Autowired
  private CloseableHttpClient httpClient;

  @Override
  public SwaggerResponse getApiDocs()
      throws InterruptedException, ExecutionException, URISyntaxException, MalformedURLException {
    RootResponse root = getRoot();
    URI baseUri = config.getBaseUri();
    String swagger = restTemplate.getForEntity(baseUri.resolve(root.getApiDocsLink()), String.class)
        .getBody();

    return new JavaSwaggerResponse(swaggerParser.parse(swagger));
  }

  @Override
  public RootResponse getRoot() throws MalformedURLException {
    URI baseUri = config.getBaseUri();
    Root root = restTemplate.getForEntity(baseUri, Root.class).getBody();

    return new RestRootResponse(httpClient, httpAsyncClient, baseUri.toURL(), root, restTemplate);
  }

  @Override
  public RyvrsCollectionResponse getRyvrsCollection() throws MalformedURLException {
    return getRoot().followRyvrsLink();
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
  public Ryvr getRyvrDirect(String name) throws Throwable {
    // instead of following the links, we are going to just construct the
    // URL and hit it directly, to ensure the correct 404 is returned
    URL contextUrl = getRyvrsCollection().getContextUrl();
    try {
      URI ryvrUri = contextUrl.toURI().resolve(name);
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
}
