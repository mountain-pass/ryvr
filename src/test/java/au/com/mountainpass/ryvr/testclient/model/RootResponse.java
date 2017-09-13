package au.com.mountainpass.ryvr.testclient.model;

import java.net.URI;

import au.com.mountainpass.ryvr.model.RyvrsCollection;

public interface RootResponse {

  void assertHasApiDocsLink();

  void assertHasRyvrsLink();

  void assertHasTitle(String title);

  RyvrsCollection followRyvrsLink();

  URI getApiDocsLink();

}
