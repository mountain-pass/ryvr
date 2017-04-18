package au.com.mountainpass.ryvr.testclient.model;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public interface RyvrResponse {

    public void assertHasItem(List<Map<String, String>> events)
            throws URISyntaxException;

    public void assertHasLinks(List<String> links);

    public void assertDoesntHaveLinks(List<String> links);

    public RyvrResponse followPrevLink() throws URISyntaxException;

}
