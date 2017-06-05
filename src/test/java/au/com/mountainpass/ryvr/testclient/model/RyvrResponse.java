package au.com.mountainpass.ryvr.testclient.model;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public interface RyvrResponse {

  public void assertDoesntHaveLinks(List<String> links);

  public void assertFromCache();

  public void assertHasItems(List<Map<String, String>> events) throws URISyntaxException;

  public void assertHasLinks(List<String> links);

  public void assertItemsHaveStructure(List<String> structure) throws URISyntaxException;

  public void assertLoadedWithin(int percentile, int maxMs);

  public void assertNotFromCache();

  public void clearMetrics();

  public RyvrResponse followLink(String rel);

  public boolean hasLink(String rel);

  public void retrieveAllEvents() throws Throwable;

}
