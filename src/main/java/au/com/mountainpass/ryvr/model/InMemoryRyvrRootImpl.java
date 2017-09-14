package au.com.mountainpass.ryvr.model;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRyvrRootImpl implements RyvrRootImpl {

  @Autowired
  private JavaSwaggerImpl swaggerImpl;

  @Autowired
  private InMemoryRyvrsCollectionImpl ryvrsCollectionImpl;

  @Override
  public SwaggerImpl getApiDocs() {
    return swaggerImpl;
  }

  @Override
  public RyvrsCollection getRyvrsCollection() {
    return new RyvrsCollection(ryvrsCollectionImpl);
  }

  @Override
  public void login(String username, String password) throws ClientProtocolException, IOException {
    // do nothing
  }

  @Override
  public void logout() {
    // do nothing
  }

}
