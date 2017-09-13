package au.com.mountainpass.ryvr.model;

import java.net.URI;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import au.com.mountainpass.ryvr.rest.LinkHeader;
import au.com.mountainpass.ryvr.rest.RestRyvrSource;

public class RestRyvrCollectionImpl extends AbstractMap<String, Ryvr>
    implements RyvrCollectionImpl {
  CloseableHttpClient httpClient;
  CookieStore cookieStore;
  CloseableHttpResponse response;
  URI rootLocation;

  public RestRyvrCollectionImpl(CloseableHttpClient httpClient, CookieStore cookieStore,
      CloseableHttpResponse response, URI rootLocation) {
    this.httpClient = httpClient;
    this.cookieStore = cookieStore;
    this.response = response;
    this.rootLocation = rootLocation;
  }

  @Override
  public Set<java.util.Map.Entry<String, Ryvr>> entrySet() {
    Header[] links = response.getHeaders("Link");
    Map<String, Ryvr> ryvrs = Arrays.stream(links).filter(link -> {
      return "item".equals(LinkHeader.extractRel(link.getValue()));
    }).collect(Collectors.toMap(header -> LinkHeader.extractTitle(header), header -> {
      URI uri = rootLocation.resolve(LinkHeader.extractUri(header));
      // TODO change the Ryvr constructor, so we can return a ryvr that we
      // have not yet resolved
      return new Ryvr(LinkHeader.extractTitle(header), 10,
          new RestRyvrSource(httpClient, uri, null, response));
    }));
    return ryvrs.entrySet();
  }

}
