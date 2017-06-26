package au.com.mountainpass.ryvr.testclient;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import au.com.mountainpass.ryvr.controllers.JsonController;
import au.com.mountainpass.ryvr.model.Root;
import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;
import au.com.mountainpass.ryvr.testclient.model.JavaRootResponse;
import au.com.mountainpass.ryvr.testclient.model.JavaSwaggerResponse;
import au.com.mountainpass.ryvr.testclient.model.RootResponse;
import au.com.mountainpass.ryvr.testclient.model.RyvrsCollectionResponse;
import au.com.mountainpass.ryvr.testclient.model.SwaggerResponse;
import cucumber.api.Scenario;
import io.swagger.parser.SwaggerParser;

public class JavaRyvrClient implements RyvrTestClient {

  @Autowired
  private JsonController router;

  private HttpServletRequest request = new MockHttpServletRequest();

  private SwaggerParser swaggerParser = new SwaggerParser();

  @Autowired
  private RyvrsCollection ryvrsCollection;

  @Override
  public SwaggerResponse getApiDocs() {
    return new JavaSwaggerResponse(
        swaggerParser.parse((String) router.getApiDocs(request, "").getBody()));
  }

  @Override
  public RootResponse getRoot() {
    return new JavaRootResponse((Root) router.getRoot(request).getBody(), router);
  }

  @Override
  public RyvrsCollectionResponse getRyvrsCollection() {
    return getRoot().followRyvrsLink();
  }

  @Override
  public Ryvr getRyvr(String name) {
    return ryvrsCollection.getRyvr(name);
  }

  @Override
  public void after(Scenario s) {
    // nothing
  }

  @Override
  public void before(Scenario scenario) {
    // nothing
  }
}
