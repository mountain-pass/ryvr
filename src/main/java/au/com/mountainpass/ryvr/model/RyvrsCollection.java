package au.com.mountainpass.ryvr.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class RyvrsCollection implements Map<String, Ryvr> {

  private static final String TITLE = "Ryvrs";

  private RyvrCollectionImpl impl;

  public RyvrsCollection(RyvrCollectionImpl impl) {
    this.impl = impl;
  }

  // public void addRyvr(Ryvr ryvr) {
  // String name = ryvr.getTitle();
  // ryvrs.put(name, ryvr);
  // links = genLinks();
  // }

  // public int getCount() {
  // return links.get("item") == null ? 0 : links.get("item").length;
  // // return ryvrs.size();
  // }

  public String getTitle() {
    return TITLE;
  }

  @Override
  public int size() {
    return impl.size();
  }

  @Override
  public boolean isEmpty() {
    return impl.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return impl.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return impl.containsValue(value);
  }

  @Override
  public Ryvr get(Object key) {
    return impl.get(key);
  }

  @Override
  public Ryvr put(String key, Ryvr value) {
    return impl.put(key, value);
  }

  @Override
  public Ryvr remove(Object key) {
    return impl.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Ryvr> m) {
    impl.putAll(m);
  }

  @Override
  public void clear() {
    impl.clear();
  }

  @Override
  public Set<String> keySet() {
    return impl.keySet();
  }

  @Override
  public Collection<Ryvr> values() {
    return impl.values();
  }

  @Override
  public Set<java.util.Map.Entry<String, Ryvr>> entrySet() {
    return impl.entrySet();
  }

}
