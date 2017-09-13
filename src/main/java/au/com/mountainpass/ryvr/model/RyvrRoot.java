package au.com.mountainpass.ryvr.model;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import au.com.mountainpass.ryvr.testclient.model.SwaggerImpl;

public class RyvrRoot {

  public static final String RELS_RYVRS_COLLECTION = "https://mountain-pass.github.io/ryvr/rels/ryvrs-collection";
  private String title;

  private RyvrRootImpl impl;

  public RyvrRoot(String title, RyvrRootImpl impl) {
    this.title = title;
    this.impl = impl;
  }

  // public RyvrRoot(String title) {
  //// super(linkingTo(linkBuilder("self", "/").withTitle("Home").build(),
  //// linkBuilder("describedby", "/api-docs").withTitle("API Docs").build(),
  //// linkBuilder(RELS_RYVRS_COLLECTION, "/ryvrs").withTitle("Ryvrs").build()));
  // this.title = title;
  // }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  public SwaggerImpl getApiDocs() throws ClientProtocolException, IOException {
    return impl.getApiDocs();
  }

  public RyvrsCollection getRyvrsCollection() {
    return impl.getRyvrsCollection();
  }

  public void login(String username, String password) throws ClientProtocolException, IOException {
    impl.login(username, password);
  }

}
