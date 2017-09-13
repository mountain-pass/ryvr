package au.com.mountainpass.ryvr.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mountainpass.ryvr.testclient.model.SwaggerImpl;

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

}
