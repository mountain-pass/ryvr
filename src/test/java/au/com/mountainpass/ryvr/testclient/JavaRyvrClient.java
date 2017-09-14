package au.com.mountainpass.ryvr.testclient;

import java.io.IOException;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.mountainpass.ryvr.model.InMemoryRyvrRootImpl;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrRoot;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.model.SwaggerImpl;
import cucumber.api.Scenario;

public class JavaRyvrClient implements RyvrTestClient {

  @Autowired
  private RyvrsCollection ryvrsCollection;

  @Autowired
  InMemoryRyvrRootImpl rootImpl;

  // @Override
  // public SwaggerImpl getApiDocs() {
  // return new JavaSwaggerResponse(
  // swaggerParser.parse((String) router.getApiDocs(request, "").getBody()));
  // }

  @Override
  public RyvrRoot getRoot() {
    return new RyvrRoot("ryvr", rootImpl);
  }

  @Override
  public RyvrsCollection getRyvrsCollection() throws ClientProtocolException, IOException {
    return getRoot().getRyvrsCollection();
  }

  @Override
  public Ryvr getRyvr(String name) throws Throwable {
    return getRyvrDirect(name);
  }

  @Override
  public void after(Scenario s) {
    // nothing
  }

  @Override
  public void before(Scenario scenario) {
    // nothing
  }

  @Override
  public Ryvr getRyvrDirect(String name, int page) throws Throwable {
    Ryvr ryvr = ryvrsCollection.get(name);
    if (ryvr != null) {
      int pageSize = ryvr.getPageSize();
      ryvr.getSource().iterator((page - 1) * pageSize);
    }
    return ryvr;
  }

  @Override
  public SwaggerImpl getApiDocs() throws Throwable {
    throw new NotImplementedException("TODO");
  }

  @Override
  public RyvrsCollection getRyvrsCollectionDirect() throws Throwable {
    return ryvrsCollection;
  }
}
