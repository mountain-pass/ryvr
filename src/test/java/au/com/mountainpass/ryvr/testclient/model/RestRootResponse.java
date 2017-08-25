package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

public class RestRootResponse implements RootResponse {

  private Root root;
  private URL contextUrl;
  private RestTemplate restTemplate;
  private CloseableHttpAsyncClient httpAsyncClient;
  private CloseableHttpClient httpClient;

  public RestRootResponse(CloseableHttpClient httpClient, CloseableHttpAsyncClient httpAsyncClient,
      URL contextUrl, Root root, RestTemplate restTemplate) {
    this.httpClient = httpClient;
    this.httpAsyncClient = httpAsyncClient;
    this.contextUrl = contextUrl;
    this.root = root;
    this.restTemplate = restTemplate;
  }

  @Override
  public void assertHasApiDocsLink() {
    RootUtil.assertHasLink(root, "API Docs");
  }

  @Override
  public void assertHasRyvrsLink() {
    RootUtil.assertHasLink(root, "Ryvrs");
  }

  @Override
  public void assertHasTitle(String title) {
    assertThat(root.getTitle(), equalTo(title));
  }

  @Override
  public RyvrsCollectionResponse followRyvrsLink() {
    try {
      String path = root.getLinks()
          .getLinkBy("https://mountain-pass.github.io/ryvr/rels/ryvrs-collection").get().getHref();
      URI ryvrsUri = contextUrl.toURI().resolve(path);
      RyvrsCollection ryvrsCollection = restTemplate.getForEntity(ryvrsUri, RyvrsCollection.class)
          .getBody();
      return new RestRyvrsCollectionResponse(httpClient, httpAsyncClient, ryvrsUri.toURL(),
          ryvrsCollection);
    } catch (MalformedURLException | URISyntaxException ex) {
      throw new NotImplementedException(ex);
    }
  }

  @Override
  public URI getApiDocsLink() {
    return root.getLinks().stream().filter(link -> {
      return link.getRel().equals("describedby");
    }).map(link -> URI.create(link.getHref())).findAny().get();
  }

}
