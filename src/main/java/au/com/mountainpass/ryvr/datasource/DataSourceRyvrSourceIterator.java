package au.com.mountainpass.ryvr.datasource;

import java.sql.SQLException;
import java.util.Iterator;

import au.com.mountainpass.ryvr.model.Record;

class DataSourceRyvrSourceIterator implements Iterator<Record> {
  /**
   * 
   */
  private final DataSourceRyvrSource dataSourceRyvrSource;

  public DataSourceRyvrSourceIterator(DataSourceRyvrSource dataSourceRyvrSource, long position) {
    try {
      this.dataSourceRyvrSource = dataSourceRyvrSource;
      // todo: handle int overflow, by moving it to max int and then using relative movements
      if (position == 0L) {
        this.dataSourceRyvrSource.getRowSet().beforeFirst();
      } else {
        this.dataSourceRyvrSource.getRowSet().absolute(Math.toIntExact(position));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public DataSourceRyvrSourceIterator(DataSourceRyvrSource dataSourceRyvrSource) {
    this.dataSourceRyvrSource = dataSourceRyvrSource;
    try {
      this.dataSourceRyvrSource.getRowSet().beforeFirst();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean hasNext() {
    try {
      return !this.dataSourceRyvrSource.getRowSet().isLast()
          && !dataSourceRyvrSource.getRowSet().isAfterLast();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Record next() {
    try {
      this.dataSourceRyvrSource.getRowSet().next();
      return this.dataSourceRyvrSource.record;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}