package au.com.mountainpass.ryvr.testclient.model;

import org.springframework.web.client.AsyncRestTemplate;

import au.com.mountainpass.ryvr.config.RyvrConfiguration;
import au.com.mountainpass.ryvr.model.RyvrsCollection;

public class RestRyvrsCollectionResponse implements RyvrsCollectionResponse {

    public RestRyvrsCollectionResponse(AsyncRestTemplate restTemplate,
            RyvrConfiguration config, RyvrsCollection body) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void assertIsEmpty() {
        // TODO Auto-generated method stub

    }

    @Override
    public void assertCount(int count) {
        // TODO Auto-generated method stub

    }

}
