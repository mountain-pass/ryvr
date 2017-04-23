package au.com.mountainpass.ryvr.testclient.model;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public interface RyvrResponse {

    public void assertHasItems(List<Map<String, String>> events)
            throws URISyntaxException;

    public void assertHasLinks(List<String> links);

    public void assertDoesntHaveLinks(List<String> links);

    public RyvrResponse followLink(String rel) throws URISyntaxException;

    public void assertItemsHaveStructure(List<String> structure)
            throws URISyntaxException;

    public void retrieveAllEvents() throws URISyntaxException;

    public boolean hasLink(String rel);

    public void assertLoadedWithin(int percentile, int maxMs);

}
