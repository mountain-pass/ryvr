package au.com.mountainpass.ryvr.config;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class HttpCacheStatusHeaderAdder implements HttpResponseInterceptor {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Override
  public void process(HttpResponse response, HttpContext context)
      throws HttpException, IOException {
    CacheResponseStatus cacheResponseStatus = (CacheResponseStatus) context
        .getAttribute(HttpCacheContext.CACHE_RESPONSE_STATUS);
    String xCacheString;
    switch (cacheResponseStatus) {
    case CACHE_HIT:
      xCacheString = "HIT";
      break;
    case VALIDATED:
      xCacheString = "VALIDATED";
      break;
    case CACHE_MISS:
    case CACHE_MODULE_RESPONSE:
      xCacheString = "MISS";
      break;
    default:
      xCacheString = cacheResponseStatus.toString();
    }
    // HttpRequestWrapper request = (HttpRequestWrapper) context
    // .getAttribute(HttpCacheContext.HTTP_REQUEST);
    // LOGGER.info("X-Cache: {}\t{}", xCacheString, request.getURI());
    response.addHeader("X-Cache", xCacheString);
    long length = response.getEntity().getContentLength();
    response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(length));
  }
}