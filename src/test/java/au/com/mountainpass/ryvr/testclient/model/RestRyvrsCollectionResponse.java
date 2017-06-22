package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.rest.RestRyvr;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrsCollectionResponse implements RyvrsCollectionResponse {

  private RyvrsCollection ryvrsCollection;
  private Traverson traverson;
  private URL contextUrl;
  private RestTemplate restTemplate;
  private CloseableHttpAsyncClient httpAsyncClient;
  private CloseableHttpClient httpClient;

  public RestRyvrsCollectionResponse(CloseableHttpClient httpClient,
      CloseableHttpAsyncClient httpAsyncClient, Traverson traverson, URL contextUrl,
      RyvrsCollection ryvrsCollection, RestTemplate restTemplate) {
    this.httpClient = httpClient;
    this.httpAsyncClient = httpAsyncClient;
    this.traverson = traverson;
    this.contextUrl = contextUrl;
    this.ryvrsCollection = ryvrsCollection;
    this.restTemplate = restTemplate;
  }

  @Override
  public void assertIsEmpty() {
    assertCount(0);
  }

  @Override
  public void assertCount(int count) {
    assertThat(ryvrsCollection.getCount(), equalTo(count));
  }

  @Override
  public void assertHasItem(List<String> names) {
    List<String> titles = Arrays.asList(ryvrsCollection.getLinks().get("item")).stream()
        .map(link -> link.getTitle()).collect(Collectors.toList());
    assertThat(titles, containsInAnyOrder(names.toArray()));
  }

  @Override
  public Ryvr followRyvrLink(String name) {
    try {
      URI ryvrUri = contextUrl.toURI().resolve("/ryvrs/" + name);
      final HttpGet httpget = new HttpGet(ryvrUri);
      httpget.reset();
      httpget.addHeader("Accept", "application/json");
      CloseableHttpResponse response = httpClient.execute(httpget);

      if (response.getStatusLine().getStatusCode() == HttpStatus.SEE_OTHER.value()) {
        httpget.reset();
        httpget.setURI(URI.create(response.getFirstHeader(HttpHeaders.LOCATION).getValue()));
        response = httpClient.execute(httpget);
      }

      return new RestRyvr(name, httpClient, httpAsyncClient, traverson, ryvrUri,
          response.getEntity(), restTemplate, response);
    } catch (URISyntaxException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new NotImplementedException();
    }
  }

  private Predicate<Link> hasName(String name) {
    return itemLink -> {
      return name.equals(itemLink.getName());
    };
  }

}
