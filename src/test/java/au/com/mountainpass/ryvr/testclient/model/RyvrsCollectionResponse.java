package au.com.mountainpass.ryvr.testclient.model;

import java.util.List;

import au.com.mountainpass.ryvr.model.Ryvr;

public interface RyvrsCollectionResponse {

  public void assertIsEmpty();

  public void assertCount(int count);

  public void assertHasItem(List<String> names);

  public Ryvr followRyvrLink(String name);

}
