package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import au.com.mountainpass.ryvr.model.Ryvr;
import de.otto.edison.hal.traverson.Traverson;

public class RestRyvrResponse extends JavaRyvrResponse {

  private Traverson traverson;
  private URL contextUrl;
  private RestTemplate restTemplate;
  private final HttpHeaders httpHeaders;

  public RestRyvrResponse(Traverson traverson, URL contextUrl, Ryvr ryvr, RestTemplate restTemplate,
      HttpHeaders httpHeaders) {
    super(ryvr);
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
    try {
      URI ryvrUri = contextUrl.toURI()
          .resolve(extractURIByRel(httpHeaders.get(HttpHeaders.LINK), rel));
      ResponseEntity<Ryvr> entityResponse = restTemplate.getForEntity(ryvrUri, Ryvr.class);
      Ryvr ryvr = entityResponse.getBody();
      receivedBytes.observe(entityResponse.getHeaders().getContentLength());
      return new RestRyvrResponse(traverson, ryvrUri.toURL(), ryvr, restTemplate,
          entityResponse.getHeaders());
    } catch (MalformedURLException | URISyntaxException exception) {
      exception.printStackTrace();
      throw new NotImplementedException(exception);
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
}
