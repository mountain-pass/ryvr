package au.com.mountainpass.ryvr.testclient.model;

import java.net.URI;

public interface RootResponse {

  void assertHasApiDocsLink();

  void assertHasRyvrsLink();

  void assertHasTitle(String title);

  RyvrsCollectionResponse followRyvrsLink();

  URI getApiDocsLink();

}
