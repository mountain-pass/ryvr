package au.com.mountainpass.ryvr.testclient.model;

import java.util.concurrent.CompletableFuture;

public interface RootResponse {

    void assertHasApiDocsLink();

    void assertHasRyvrsLink();

    void assertHasTitle(String title);

    CompletableFuture<RyvrsCollectionResponse> followRyvrsLink();

}
