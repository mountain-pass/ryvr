package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mock.web.MockHttpServletRequest;

import au.com.mountainpass.ryvr.controllers.JsonController;
import au.com.mountainpass.ryvr.model.RyvrRoot;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

public class JavaRootResponse implements RootResponse {

  private RyvrRoot root;
  private JsonController router;

  private HttpServletRequest request = new MockHttpServletRequest();

  public JavaRootResponse(RyvrRoot root, JsonController router) {
    this.root = root;
    this.router = router;
  }

  @Override
  public void assertHasApiDocsLink() {
    RootUtil.assertHasLink(root, "API Docs");
  }

  @Override
  public void assertHasRyvrsLink() {
    RootUtil.assertHasLink(root, "Ryvrs");
  }

  @Override
  public void assertHasTitle(String title) {
    assertThat(root.getTitle(), equalTo(title));
  }

  @Override
  public RyvrsCollection followRyvrsLink() {
    return new JavaRyvrsCollectionResponse(
        (RyvrsCollection) router.getRyvrsCollection(request).getBody());
  }

  @Override
  public URI getApiDocsLink() {
    return root.getLinks().stream().filter(link -> {
      return link.getRel().equals("describedby");
    }).map(link -> URI.create(link.getHref())).findAny().get();
  }

}
