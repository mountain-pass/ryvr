package au.com.mountainpass.ryvr.model;

import java.util.Iterator;

public abstract class RyvrSource implements Iterable<Record> {

  public RyvrSource() {
  }

  @Override
  public abstract Iterator<Record> iterator();

  public abstract Iterator<Record> iterator(long position);

  public abstract long getCount();

  public abstract String[] getFieldNames();

}
