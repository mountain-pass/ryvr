package au.com.mountainpass.ryvr.testclient.model;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RyvrsCollectionResponse {

    public void assertIsEmpty();

    public void assertCount(int count);

    public void assertHasItem(List<String> names);

    public CompletableFuture<RyvrResponse> followEmbeddedRyvrLink(String name);

}
