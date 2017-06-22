package au.com.mountainpass.ryvr.datasource;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

import au.com.mountainpass.ryvr.model.Field;
import au.com.mountainpass.ryvr.model.Link;
import au.com.mountainpass.ryvr.model.Record;
import au.com.mountainpass.ryvr.model.Ryvr;

public class DataSourceRyvr extends Ryvr {
  @JsonIgnore
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Min(1)
  private int pageSize = 10; // as of 2017/05/02, optimal page size is
  // 2048 when using h2
  @JsonIgnore
  private JdbcTemplate jt;
  @JsonIgnore
  private SqlRowSet rowSet;
  private String[] columnNames;
  private String countQuery;
  private String rowsQuery;
  private String table;
  private String orderedBy;
  private long count = -1;

  private int[] columnTypes;

  public DataSourceRyvr(String title, JdbcTemplate jt, String catalog, String table,
      String orderedBy, int pageSize) throws SQLException {
    super(title);
    this.table = table;
    this.orderedBy = orderedBy;
    this.jt = jt;
    Connection connection = this.jt.getDataSource().getConnection();
    connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
    this.pageSize = pageSize;
    String identifierQuoteString = connection.getMetaData().getIdentifierQuoteString();
    String catalogSeparator = connection.getMetaData().getCatalogSeparator();

    // TODO: combine count query and row query
    countQuery = generateCountQuery(catalog, table, identifierQuoteString, catalogSeparator);
    rowsQuery = generateRowQuery(catalog, table, orderedBy, identifierQuoteString,
        catalogSeparator);
    connection.close();
  }

  public String generateRowQuery(String database, String table, String orderedBy,
      String identifierQuoteString, String catalogSeparator) {
    return "select * from " + identifierQuoteString + database + identifierQuoteString
        + catalogSeparator + identifierQuoteString + table + identifierQuoteString + " ORDER BY "
        + identifierQuoteString + database + identifierQuoteString + catalogSeparator
        + identifierQuoteString + table + identifierQuoteString + "." + identifierQuoteString
        + orderedBy + identifierQuoteString + " ASC";
  }

  public String generateCountQuery(String database, String table, String identifierQuoteString,
      String catalogSeparator) {
    return "select count(*) from " + identifierQuoteString + database + identifierQuoteString
        + catalogSeparator + identifierQuoteString + table + identifierQuoteString;
  }

  @Override
  public void refresh() {
    refreshPage(-1L);
  }

  public void embeddedToJson(StringBuilder builder) {
    builder.append("{\"item\":[");
    for (int i = 0; i < pageSize; ++i) {
      builder.append("{");
      for (int j = 0; j < columnNames.length; ++j) {
        if (j != 0) {
          builder.append(",");
        }
        builder.append("\"");
        builder.append(columnNames[j]);
        builder.append("\":");
        Object value = rowSet.getObject(j + 1);
        if (value instanceof String) {
          builder.append("\"");
          builder.append(value);
          builder.append("\"");
        } else {
          builder.append(value);
        }
      }
      builder.append("}");
      if (!rowSet.next()) {
        break;
      }
      if (i + 1 < pageSize) {
        builder.append(",");
      }
    }
    builder.append("]}");
  }

  @Override
  public boolean refreshPage(long requestedPage) {
    if (pages < 0L || requestedPage < 0L || requestedPage >= pages) {
      count = jt.queryForObject(countQuery, Long.class);
      pages = ((count - 1L) / pageSize) + 1L;
    }
    if (requestedPage > pages) {
      throw new IndexOutOfBoundsException();
    }

    page = requestedPage < 0L ? pages : requestedPage;

    return page != pages;
  }

  public void refreshRowSet() {
    if (rowSet == null || page == pages) {
      jt.setFetchSize(pageSize);
      rowSet = jt.queryForRowSet(rowsQuery);
      columnNames = rowSet.getMetaData().getColumnNames();
      columnTypes = new int[columnNames.length];
      for (int i = 0; i < columnNames.length; ++i) {
        columnTypes[i] = rowSet.getMetaData().getColumnType(i + 1);
      }

    }
  }

  @JsonIgnore
  @Override
  public Map<String, Link[]> getLinks() {
    Map<String, Link[]> links = new HashMap<>();
    links.put("current", new Link[] { new Link("/ryvrs/" + getTitle()) });
    links.put("self", new Link[] { new Link("/ryvrs/" + getTitle() + "?page=" + page) });
    links.put("first", new Link[] { new Link("/ryvrs/" + getTitle() + "?page=1") });
    if (page > 1) {
      links.put("prev", new Link[] { new Link("/ryvrs/" + getTitle() + "?page=" + (page - 1L)) });
    }
    if (page < pages) {
      links.put("next", new Link[] { new Link("/ryvrs/" + getTitle() + "?page=" + (page + 1L)) });
    } else {
      links.put("last", new Link[] { new Link("/ryvrs/" + getTitle() + "?page=" + (pages)) });
    }
    return links;
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public String getEtag() {
    if (getPage() < 0L || getPage() == pages) {
      return Long.toHexString(count) + "." + Long.toHexString(pageSize);
    } else {
      return Long.toHexString(page) + "." + Long.toHexString(pageSize);
    }
  }

  @Override
  public void toJson(OutputStream outputStream) throws IOException {
    refreshRowSet();

    // hmmm... what happens when there are more rows than MAX_INT?
    rowSet.absolute((int) ((page - 1L) * pageSize + 1L));
    // toJsonWithBuilder();
    toJsonWithWriter(outputStream);

  }

  private ByteArrayOutputStream baos = new ByteArrayOutputStream(pageSize * 1024);
  private Writer writer = new BufferedWriter(new OutputStreamWriter(baos), pageSize * 1024);

  public void toJsonWithWriter(OutputStream outputStream) throws IOException {
    baos.reset();
    writer.write("{\"title\":\"", 0, 10);
    writer.write(getTitle());
    writer.write("\",\"page\":", 0, 9);
    writer.write(Long.toString(getPage()));
    writer.write(",\"pageSize\":", 0, 12);
    writer.write(Integer.toString(getPageSize()));
    if (getPage() == getPages()) {
      writer.write(",\"count\":", 0, 9);
      writer.write(Long.toString(getCount()));
    }
    writer.write(",\"columns\":[", 0, 12);
    for (int i = 0; i < columnNames.length; ++i) {
      if (i != 0) {
        writer.write(',');
      }
      writer.write('"');
      writer.write(columnNames[i]);
      writer.write('"');
    }
    writer.write("],\"rows\":[", 0, 10);

    for (int i = 0; i < pageSize; ++i) {
      if (i != 0) {
        writer.write(',');
      }
      writer.write('[');
      for (int j = 0; j < columnNames.length; ++j) {
        if (j != 0) {
          writer.write(',');
        }
        String value = rowSet.getString(j + 1);
        if (columnTypes[j] == Types.VARCHAR) {
          writer.write('"');
          writer.write(value);
          writer.write('"');
        } else {
          writer.write(value);
        }
      }
      writer.write(']');
      if (!rowSet.next()) {
        break;
      }
    }
    writer.write(']');
    writer.write('}');
    writer.flush();

    outputStream.write(baos.toByteArray());
    outputStream.flush();
  }

  private class RyvrIterator implements Iterator<Record> {
    long position;

    @Override
    public boolean hasNext() {
      if (position == count - 1 || count < 0) {
        long newCount = jt.queryForObject(countQuery, Long.class);
        if (newCount != count) {
          count = newCount;
          pages = ((count - 1L) / pageSize) + 1L;
          refreshRowSet();
        }
      }
      return position < count;
    }

    @Override
    public Record next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      final Record rval = new Record() {

        private long position;

        @Override
        public int size() {
          if (columnNames == null) {
            refreshRowSet();
          }
          return columnNames.length;
        }

        @Override
        public Field getField(int fieldIndex) {
          final Field field = new Field() {

            private int fieldIndex;

            @Override
            public Object getValue() {
              rowSet.absolute((int) position);
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
        public void setPosition(long position) {
          this.position = position;
        }
      };
      rval.setPosition(++position);
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
    ryvrIterator.position = 0;
    return ryvrIterator;
  }

  @Override
  public long getCount() {
    if (count == -1L) {
      refresh();
    }
    return count;
  }

  @Override
  public String[] getFieldNames() {
    return columnNames;
  }
}
