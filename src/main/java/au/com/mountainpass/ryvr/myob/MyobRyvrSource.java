package au.com.mountainpass.ryvr.myob;

import java.util.Iterator;

import org.apache.commons.lang3.NotImplementedException;

import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.RyvrSource;

public class MyobRyvrSource extends RyvrSource {

  @Override
  public Iterator<Record> iterator() {
    throw new NotImplementedException("TODO");
  }

  @Override
  public Iterator<Record> iterator(long position) {
    throw new NotImplementedException("TODO");
  }

  @Override
  public String[] getFieldNames() {
    throw new NotImplementedException("TODO");
  }

  @Override
  public void refresh() {
    throw new NotImplementedException("TODO");
  }

  @Override
  public long getRecordsRemaining(long fromPosition) {
    throw new NotImplementedException("TODO");
  }

  @Override
  public boolean isLoaded(long page) {
    throw new NotImplementedException("TODO");
  }

}
