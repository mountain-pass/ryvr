package au.com.mountainpass.ryvr.model;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class RyvrSource implements Iterable<Record> {

  public RyvrSource() {
  }

  // public abstract Iterator<Record> iterator(long position);

  // public abstract long longSize();
  //
  // @Override
  // public int size() {
  // return (int) longSize();
  // }

  public abstract Iterator<Record> iterator(long position);

  public abstract String[] getFieldNames();

  public abstract void refresh();

  public Stream<Record> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  public Stream<Record> stream(long position) {
    return StreamSupport.stream(spliterator(position), false);
  }

  public Spliterator<Record> spliterator(long position) {
    return Spliterators.spliteratorUnknownSize(iterator(position), 0);
  }

  public abstract long getRecordsRemaining(long fromPosition);

  public abstract boolean isLoaded(long page);

}
