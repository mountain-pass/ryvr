package au.com.mountainpass.ryvr.testclient.model;

import java.util.List;

public interface RyvrsCollectionResponse {

    void assertIsEmpty();

    void assertCount(int count);

    void assertHasEmbedded(List<String> names);

}
