package au.com.mountainpass.ryvr.testclient.model;

import java.util.List;
import java.util.Map;

public interface RyvrResponse {

    void assertHasEmbedded(List<Map<String, String>> events);

    void assertHasLinks(List<String> links);

    void assertDoesntHaveLinks(List<String> links);

}
