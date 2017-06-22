package au.com.mountainpass.ryvr.testclient.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import au.com.mountainpass.ryvr.model.Ryvr;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

public class JavaRyvrsCollectionResponse implements RyvrsCollectionResponse {

  private RyvrsCollection ryvrsCollection;

  public JavaRyvrsCollectionResponse(RyvrsCollection ryvrsCollection) {
    this.ryvrsCollection = ryvrsCollection;
  }

  @Override
  public void assertIsEmpty() {
    assertThat(ryvrsCollection.getCount(), equalTo(0));

  }

  @Override
  public void assertCount(int count) {
    assertThat(ryvrsCollection.getCount(), equalTo(count));
  }

  @Override
  public void assertHasItem(List<String> names) {
    Set<String> titles = ryvrsCollection.getRyvrs().keySet();
    assertThat(titles, containsInAnyOrder(names.toArray()));
  }

  @Override
  public Ryvr followRyvrLink(String name) {
    Ryvr ryvr = ryvrsCollection.getRyvr(name);
    return ryvr;
  }

}
