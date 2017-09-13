package au.com.mountainpass.ryvr.model;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import au.com.mountainpass.ryvr.rest.LinkHeader;
import au.com.mountainpass.ryvr.testclient.model.SwaggerImpl;

public class RestRyvrRootImpl implements RyvrRootImpl {

  private CloseableHttpResponse response;
  private CloseableHttpClient httpClient;
  private CookieStore cookieStore;
  private URI rootLocation;

  public RestRyvrRootImpl(CloseableHttpClient httpClient, CookieStore cookieStore,
      CloseableHttpResponse response, URI rootLocation) {
    this.httpClient = httpClient;
    this.cookieStore = cookieStore;
    this.response = response;
    this.rootLocation = rootLocation;
  }

  /*
   * (non-Javadoc)
   * 
   * @see au.com.mountainpass.ryvr.model.RyvrRootImpl#getApiDocs()
   */
  @Override
  public SwaggerImpl getApiDocs() throws ClientProtocolException, IOException {
    String uri = LinkHeader.extractUriByRel(response.getHeaders(HttpHeaders.LINK), "describedby");
    HttpGet request = new HttpGet(rootLocation.resolve(uri));
    request.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
    CloseableHttpResponse response = httpClient.execute(request);
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      return new RestSwaggerImpl(response);
    } else {
      throw new NotImplementedException("TODO: handle " + response.getStatusLine().toString());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see au.com.mountainpass.ryvr.model.RyvrRootImpl#getRyvrsCollection()
   */
  @Override
  public RyvrsCollection getRyvrsCollection() throws ClientProtocolException, IOException {
    String uri = LinkHeader.extractUriByRel(response.getHeaders(HttpHeaders.LINK),
        RyvrsCollection.RELS_RYVRS_COLLECTION);
    HttpGet request = new HttpGet(rootLocation.resolve(uri));
    request.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
    CloseableHttpResponse response = httpClient.execute(request);
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      return new RyvrsCollection(
          new RestRyvrCollectionImpl(httpClient, cookieStore, response, rootLocation));
    } else {
      throw new NotImplementedException("TODO: handle " + response.getStatusLine().toString());
    }
  }

  @Override
  public void login(String username, String password) throws ClientProtocolException, IOException {

    final HttpPost request = new HttpPost();
    request.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
    request.setHeader("X-XSRF-TOKEN", cookieStore.getCookies().stream()
        .filter(cookie -> cookie.getName().equals("XSRF-TOKEN")).findAny().get().getValue());
    request.reset();
    request.setURI(rootLocation);
    List<NameValuePair> params = new ArrayList<>(2);
    params.add(new BasicNameValuePair("username", username));
    params.add(new BasicNameValuePair("password", password));
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
    request.setEntity(entity);
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
        // TODO: check we are authenticated
      } else {
        throw new NotImplementedException("TODO: handle " + response.getStatusLine().toString());
      }
    }

  }

}
