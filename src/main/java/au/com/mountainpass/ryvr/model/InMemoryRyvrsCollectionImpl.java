package au.com.mountainpass.ryvr.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRyvrsCollectionImpl extends HashMap<String, Ryvr>
    implements RyvrCollectionImpl {

  private static final String TITLE = "Ryvrs";

  public InMemoryRyvrsCollectionImpl(@Autowired Map<String, Ryvr> ryvrs) {
    super(ryvrs);
  }

  public String getTitle() {
    return TITLE;
  }

  // private Map<String, Link[]> genLinks() {
  // Map<String, Link[]> rval = new HashMap<>();
  // rval.put("self", new Link[] { Link.fromUri(URI.create("/ryvrs")).title(getTitle()).build() });
  // if (!ryvrs.isEmpty()) {
  // Link[] items = ryvrs.keySet().stream()
  // .map(key -> Link.fromUri(URI.create("/ryvrs/" + key + "?page=1")).title(key).build())
  // .collect(Collectors.toList()).toArray(new Link[] {});
  // rval.put("item", items);
  // }
  // return rval;
  // }

}
