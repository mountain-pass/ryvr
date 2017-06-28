package au.com.mountainpass.ryvr.model;

import java.util.AbstractList;
import java.util.Iterator;

public abstract class RyvrSource extends AbstractList<Record> {

  public RyvrSource() {
  }

  public abstract Iterator<Record> iterator(long position);

  public abstract long longSize();

  @Override
  public int size() {
    return (int) longSize();
  }

  public Iterator<Record> listIterator(long position) {
    return iterator(position);
  }

  public abstract String[] getFieldNames();

}
