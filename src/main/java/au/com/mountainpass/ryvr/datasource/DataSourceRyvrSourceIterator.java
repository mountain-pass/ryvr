package au.com.mountainpass.ryvr.datasource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import au.com.mountainpass.ryvr.model.Record;

class DataSourceRyvrSourceIterator implements Iterator<Record> {
  /**
   * 
   */
  // private final DataSourceRyvrSource dataSourceRyvrSource;
  private final ResultSet rowSet;
  private final Record record;

  public DataSourceRyvrSourceIterator(DataSourceRyvrSource dataSourceRyvrSource, long position) {
    // this.dataSourceRyvrSource = dataSourceRyvrSource;
    record = new DataSourceRecord(dataSourceRyvrSource);
    try {
      this.rowSet = dataSourceRyvrSource.getRowSet();
      // this.dataSourceRyvrSource = dataSourceRyvrSource;
      // todo: handle int overflow, by moving it to max int and then using relative movements
      if (position == 0L) {
        rowSet.beforeFirst();
      } else {
        rowSet.absolute(Math.toIntExact(position));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public DataSourceRyvrSourceIterator(DataSourceRyvrSource dataSourceRyvrSource) {
    // this.dataSourceRyvrSource = dataSourceRyvrSource;
    record = new DataSourceRecord(dataSourceRyvrSource);
    try {
      ResultSet rs = dataSourceRyvrSource.getRowSet();
      if (rs == null) {
        rs = dataSourceRyvrSource.refreshRowSet();
      } else {
        rs.beforeFirst();
      }
      rowSet = rs;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean hasNext() {
    try {
      return !(rowSet.isLast() || rowSet.isAfterLast()
          || (rowSet.getRow() == 0 && !rowSet.isBeforeFirst()));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Record next() {
    try {
      if (rowSet.next()) {
        return record;
      } else {
        throw new NoSuchElementException();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}