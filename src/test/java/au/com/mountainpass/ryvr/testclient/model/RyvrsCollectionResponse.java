package au.com.mountainpass.ryvr.testclient.model;

import java.util.List;

public interface RyvrsCollectionResponse {

    public void assertIsEmpty();

    public void assertCount(int count);

    public void assertHasItem(List<String> names);

    public RyvrResponse followEmbeddedRyvrLink(String name);

}
