package au.com.mountainpass.ryvr.testclient;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.Ryvr;
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
}
