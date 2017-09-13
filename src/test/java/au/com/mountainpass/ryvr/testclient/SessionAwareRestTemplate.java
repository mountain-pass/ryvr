package au.com.mountainpass.ryvr.testclient;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class SessionAwareRestTemplate extends RestTemplate {

  private String session;
  private String xsrfToken;

  public SessionAwareRestTemplate(HttpComponentsClientHttpRequestFactory httpClientFactory) {
    super(httpClientFactory);
  }

  @Override
  protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
      ResponseExtractor<T> responseExtractor) throws RestClientException {
    T response = super.doExecute(url, method, requestCallback, responseExtractor);

    return response;
  }

  public <T> T login(String username, String password, URI loginUri) {
    if (xsrfToken == null || session == null) {
      // do a get on / and extract the XSRF-TOKEN and JSESSIONID from the COOKIE
      // response header
      HttpHeaders headers = super.headForHeaders(loginUri);
      List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
    }
    throw new NotImplementedException(
        "TODO: call login service and then check if authenticated via the /user service");
  }

  public <T> T logout() {
    throw new NotImplementedException("TODO: call /logout and clear cookies");
  }
}
