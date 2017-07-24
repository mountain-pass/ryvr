package au.com.mountainpass.ryvr.config;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class RyvrRedirectStrategy extends DefaultRedirectStrategy {

  public static final RyvrRedirectStrategy INSTANCE = new RyvrRedirectStrategy();

  @Override
  public boolean isRedirected(final HttpRequest request, final HttpResponse response,
      final HttpContext context) throws ProtocolException {
    final int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_SEE_OTHER) {
      return true;
    }
    return super.isRedirected(request, response, context);
  }

}
