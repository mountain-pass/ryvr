package au.com.mountainpass.ryvr.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.RyvrSource;

public class DataSourceRyvrSource extends RyvrSource {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private JdbcTemplate jt;
  private SqlRowSet rowSet;
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
    return "select *, (select count(*) from " + identifierQuoteString + database
        + identifierQuoteString + catalogSeparator + identifierQuoteString + table
        + identifierQuoteString + ") from " + identifierQuoteString + database
        + identifierQuoteString + catalogSeparator + identifierQuoteString + table
        + identifierQuoteString + " ORDER BY " + identifierQuoteString + database
        + identifierQuoteString + catalogSeparator + identifierQuoteString + table
        + identifierQuoteString + "." + identifierQuoteString + orderedBy + identifierQuoteString
        + " ASC";
  }

  public void refreshCount() {
    int currentRow = getRowSet().getRow();
    refreshRowSet(currentRow);
    if (rowSet.isBeforeFirst()) {
      rowSet.first();
    }
    count = rowSet.getLong(columnNames.length + 1);
  }

  public SqlRowSet getRowSet() {
    if (rowSet == null) {
      rowSet = jt.queryForRowSet(rowsQuery);
      columnNames = getRowSet().getMetaData().getColumnNames();
      columnNames = Arrays.copyOfRange(columnNames, 0, columnNames.length - 1);
    }
    return rowSet;
  }

  public void refreshRowSet(long position) {
    rowSet = jt.queryForRowSet(rowsQuery);
    columnNames = getRowSet().getMetaData().getColumnNames();
    columnNames = Arrays.copyOfRange(columnNames, 0, columnNames.length - 1);
    // columnTypes = new int[columnNames.length];
    // for (int i = 0; i < columnNames.length; ++i) {
    // columnTypes[i] = rowSet.getMetaData().getColumnType(i + 1);
    // }
    if (position != 0) {
      rowSet.absolute((int) position);
    }
  }

  // @Override
  // public int getPageSize() {
  // return pageSize;
  // }

  // @Override
  // public String getEtag() {
  // if (getPage() < 0L || getPage() == pages) {
  // return Long.toHexString(count) + "." + Long.toHexString(pageSize);
  // } else {
  // return Long.toHexString(page) + "." + Long.toHexString(pageSize);
  // }
  // }

  private class RyvrIterator implements Iterator<Record> {

    @Override
    public boolean hasNext() {
      if (getRowSet().getRow() == count || count < 0) {
        refreshCount();
      }
      return getRowSet().getRow() < count;
    }

    @Override
    public Record next() {
      if (!hasNext()) {
        throw new NoSuchElementException("Current Row: " + getRowSet().getRow());
      }
      final Record rval = new Record() {

        @Override
        public int size() {
          // subtract 1 because each record also contains the count
          return getFieldNames().length;
        }

        @Override
        public Field getField(int fieldIndex) {
          final Field field = new Field() {

            private int fieldIndex;

            @Override
            public Object getValue() {
              // rowSet.absolute((int) position);
              return rowSet.getObject(fieldIndex + 1);
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
          field.setFieldIndex(fieldIndex);
          return field;
        }

        @Override
        public void setPosition(long l) {
          throw new NotImplementedException("TODO");
        }

      };
      getRowSet().next();
      return rval;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachRemaining(Consumer<? super Record> consumer) {
      if (!hasNext()) {
        return;
      }
      while (hasNext()) {
        consumer.accept(next());
      }
    }

  }

  @Override
  public Iterator<Record> iterator() {
    final RyvrIterator ryvrIterator = new RyvrIterator();
    getRowSet().beforeFirst();
    return ryvrIterator;
  }

  @Override
  public Iterator<Record> iterator(long position) {
    final RyvrIterator ryvrIterator = new RyvrIterator();
    if (position == 0) {
      getRowSet().beforeFirst();
    } else {
      getRowSet().absolute((int) position);
    }
    return ryvrIterator;
  }

  @Override
  public long longSize() {
    if (count == -1L) {
      refreshCount();
    }
    return count;
  }

  @Override
  public Record get(int index) {
    return iterator(index + 1).next();
  }

  @Override
  public String[] getFieldNames() {
    if (columnNames == null) {
      columnNames = getRowSet().getMetaData().getColumnNames();
    }
    return columnNames;

  }

}
