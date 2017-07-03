package au.com.mountainpass.ryvr.config;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(value = { "restApi" })
public class HttpDelayConcurrent implements HttpResponseInterceptor, HttpRequestInterceptor {

  private static final String URI_KEY = "URI";
  ConcurrentHashMap<String, LongAdder> pending = new ConcurrentHashMap<>();

  @Override
  public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
    String uri = request.getRequestLine().getUri();
    pending.putIfAbsent(uri, new LongAdder());
    while (pending.get(uri).longValue() > 0) {
      Thread.yield();
    }
    context.setAttribute(URI_KEY, uri);
    pending.get(uri).increment();
  }

  @Override
  public void process(HttpResponse response, HttpContext context)
      throws HttpException, IOException {
    String uri = (String) context.getAttribute(URI_KEY);
    pending.get(uri).decrement();
  }

}
