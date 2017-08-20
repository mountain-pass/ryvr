package au.com.mountainpass.ryvr.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.RyvrSource;

public class DataSourceRyvrSource extends RyvrSource {
  public static final Logger LOGGER = LoggerFactory.getLogger(DataSourceRyvrSource.class);

  private ResultSet rowSet;
  String[] columnNames;
  private PreparedStatement rowsQuery;

  public DataSourceRyvrSource(DataSource dataSource, String query) throws SQLException {

    try (Connection connection = dataSource.getConnection()) {

      connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);

      rowsQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
          ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
      LOGGER.info("ROWS QUERY: {}", rowsQuery);
      refreshRowSet();
      findFieldNames();
    }
  }

  public void refreshCount() {
    try {
      // LOGGER.info("Refreshing - Current Row: {}", rowSet.getRow());
      int currentRow = 0;
      if (rowSet != null) {
        currentRow = rowSet.getRow();
      }
      refreshRowSet();
      rowSet.last();
      if (currentRow == 0) {
        rowSet.beforeFirst();
      } else {
        rowSet.absolute(currentRow);
      }
      // LOGGER.info("Refreshed - Current Row: {}", rowSet.getRow());
      // LOGGER.info("Refreshed - Total Rows: {}", count);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public ResultSet getRowSet() throws SQLException {
    return rowSet == null ? rowSet = rowsQuery.executeQuery() : rowSet;
  }

  ResultSet refreshRowSet() throws SQLException {
    rowSet = rowsQuery.executeQuery();
    return rowSet;
  }

  // @Override
  // public long longSize() {
  // if (count == -1L) {
  // refreshCount();
  // }
  // return count;
  // }

  @Override
  public String[] getFieldNames() {
    return columnNames;

  }

  private String[] findFieldNames() {
    if (columnNames == null) {
      try {
        if (rowSet == null) {
          rowSet = rowsQuery.executeQuery();
        }
        ResultSetMetaData metaData = rowsQuery.getMetaData();
        int columnCount = metaData.getColumnCount();
        columnNames = new String[columnCount];
        for (int i = 0; i < columnCount; ++i) {
          columnNames[i] = metaData.getColumnName(i + 1);
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
    return columnNames;
  }

  @Override
  public void refresh() {
    try {
      refreshRowSet();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Iterator<Record> iterator() {
    return new DataSourceRyvrSourceIterator(this);
  }

  @Override
  public Iterator<Record> iterator(long position) {
    return new DataSourceRyvrSourceIterator(this, position);
  }

  @Override
  public long getRecordsRemaining(long fromPosition) {
    try {
      if (rowSet == null) {
        refreshRowSet();
      }
      rowSet.last();
      long records = rowSet.getRow() - fromPosition;
      if (fromPosition == 0L) {
        rowSet.beforeFirst();
      } else {
        rowSet.absolute((int) fromPosition);
      }
      return records;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isLoaded(long page) {
    return rowSet != null;
  }

}
