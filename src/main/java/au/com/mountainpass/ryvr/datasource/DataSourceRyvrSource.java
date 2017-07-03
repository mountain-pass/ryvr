package au.com.mountainpass.ryvr.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.RyvrSource;

public class DataSourceRyvrSource extends RyvrSource {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private JdbcTemplate jt;
  private ThreadLocal<SqlRowSet> rowSet = new ThreadLocal<>();
  private String[] columnNames;
  private String rowsQuery;
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
    connection.close();
  }

  public String generateRowQuery(String database, String table, String orderedBy,
      String identifierQuoteString, String catalogSeparator) {
    return "select " + identifierQuoteString + database + identifierQuoteString + catalogSeparator
        + identifierQuoteString + table + identifierQuoteString + ".* from " + identifierQuoteString
        + database + identifierQuoteString + catalogSeparator + identifierQuoteString + table
        + identifierQuoteString + " ORDER BY " + identifierQuoteString + database
        + identifierQuoteString + catalogSeparator + identifierQuoteString + table
        + identifierQuoteString + "." + identifierQuoteString + orderedBy + identifierQuoteString
        + " ASC";
  }

  public void refreshCount() {
    int currentRow = 0;
    SqlRowSet localRowSet = rowSet.get();
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
  }

  public SqlRowSet getRowSet() {
    SqlRowSet localRowSet = rowSet.get();
    if (localRowSet == null) {
      localRowSet = jt.queryForRowSet(rowsQuery);
      rowSet.set(localRowSet);
    }
    return localRowSet;
  }

  public void refreshRowSet(long position) {
    SqlRowSet localRowSet = refreshRowSet();
    if (position != 0) {
      localRowSet.absolute((int) position);
    }
  }

  private SqlRowSet refreshRowSet() {
    rowSet.set(null);
    return getRowSet();
  }

  @Override
  public long longSize() {
    if (count == -1L) {
      refreshCount();
    }
    return count;
  }

  final private Field field = new Field() {

    private int fieldIndex;

    @Override
    public Object getValue() {
      // plus one because the first column is column 1, not 0.
      return getRowSet().getObject(fieldIndex + 1);
    }

    @Override
    public String getName() {
      return columnNames[fieldIndex];
    }

    @Override
    public void setFieldIndex(int fieldIndex) {
      this.fieldIndex = fieldIndex;
    }
  };

  final private Record record = new Record() {

    @Override
    public int size() {
      return getFieldNames().length;
    }

    @Override
    public Field getField(int fieldIndex) {
      field.setFieldIndex(fieldIndex);
      return field;
    }

    @Override
    public void setPosition(long l) {
      throw new NotImplementedException("TODO");
    }

  };

  @Override
  public Record get(int index) {
    while (true) {
      try {
        getRowSet().absolute(index + 1);
        return record;
      } catch (InvalidResultSetAccessException e) {
        LOGGER.error("Error getting record", e);
        refreshRowSet();
      }
    }
  }

  @Override
  public String[] getFieldNames() {
    if (columnNames == null) {
      columnNames = getRowSet().getMetaData().getColumnNames();
    }
    return columnNames;

  }

}
