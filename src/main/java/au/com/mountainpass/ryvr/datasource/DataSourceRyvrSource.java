package au.com.mountainpass.ryvr.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.RyvrSource;

public class DataSourceRyvrSource extends RyvrSource {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private JdbcTemplate jt;
  private ResultSet rowSet;
  String[] columnNames;
  private PreparedStatement rowsQuery;
  // private String rowsQuery;
  private String table;
  private String orderedBy;
  private long count = -1;

  private int[] columnTypes;

  public DataSourceRyvrSource(JdbcTemplate jt, String catalog, String table, String orderedBy,
      int pageSize) throws SQLException {
    this.table = table;
    this.orderedBy = orderedBy;
    this.jt = jt;
    Connection connection = this.jt.getDataSource().getConnection();
    connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
    String identifierQuoteString = connection.getMetaData().getIdentifierQuoteString();
    String catalogSeparator = connection.getMetaData().getCatalogSeparator();

    rowsQuery = generateRowQuery(catalog, table, orderedBy, identifierQuoteString,
        catalogSeparator);
    LOGGER.info("ROWS QUERY: {}", rowsQuery);
    connection.close();
  }

  public PreparedStatement generateRowQuery(String database, String table, String orderedBy,
      String identifierQuoteString, String catalogSeparator) throws SQLException {
    String statement = "select " + identifierQuoteString + database + identifierQuoteString
        + catalogSeparator + identifierQuoteString + table + identifierQuoteString + ".* from "
        + identifierQuoteString + database + identifierQuoteString + catalogSeparator
        + identifierQuoteString + table + identifierQuoteString + " ORDER BY "
        + identifierQuoteString + database + identifierQuoteString + catalogSeparator
        + identifierQuoteString + table + identifierQuoteString + "." + identifierQuoteString
        + orderedBy + identifierQuoteString + " ASC";
    return jt.getDataSource().getConnection().prepareStatement(statement,
        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,
        ResultSet.HOLD_CURSORS_OVER_COMMIT);
  }

  public void refreshCount() {
    // LOGGER.info("Refreshing - Current Row: {}", getRowSet().getRow());
    try {
      int currentRow = 0;
      ResultSet localRowSet = rowSet;
      if (localRowSet != null) {
        currentRow = localRowSet.getRow();
      }
      localRowSet = refreshRowSet();
      localRowSet.last();
      count = localRowSet.getRow();
      if (currentRow == 0) {
        localRowSet.beforeFirst();
      } else {
        localRowSet.absolute(currentRow);
      }
      // LOGGER.info("Refreshed - Current Row: {}", getRowSet().getRow());
      // LOGGER.info("Refreshed - Total Rows: {}", count);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public ResultSet getRowSet() throws SQLException {
    ResultSet localRowSet = rowSet;
    if (localRowSet == null) {
      localRowSet = rowsQuery.executeQuery();
      rowSet = localRowSet;
    }
    return localRowSet;
  }

  private ResultSet refreshRowSet() throws SQLException {
    rowSet = null;
    return getRowSet();
  }

  // @Override
  // public long longSize() {
  // if (count == -1L) {
  // refreshCount();
  // }
  // return count;
  // }

  final Field field = new DataSourceField(this);

  final Record record = new DataSourceRecord(this);

  @Override
  public String[] getFieldNames() {
    if (columnNames == null) {
      try {
        ResultSetMetaData metaData = getRowSet().getMetaData();
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
    refreshCount();
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
      getRowSet().last();
      long records = getRowSet().getRow() - fromPosition;
      if (fromPosition == 0L) {
        getRowSet().beforeFirst();
      } else {
        getRowSet().absolute((int) fromPosition);
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
