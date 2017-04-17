package au.com.mountainpass.ryvr.testclient.model;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public interface RyvrResponse {

    void assertHasItem(List<Map<String, String>> events)
            throws URISyntaxException;

    void assertHasLinks(List<String> links);

    void assertDoesntHaveLinks(List<String> links);

}
