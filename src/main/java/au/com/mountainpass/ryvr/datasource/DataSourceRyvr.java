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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import au.com.mountainpass.ryvr.model.Link;
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
  private long count = 0;

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

  @JsonProperty("_links")
  @JsonRawValue
  public String getRawLinks() {
    StringBuilder builder = new StringBuilder();
    builder.append("{");

    builder.append("\"self\": { \"href\": \"/ryvrs/");
    builder.append(getTitle());
    builder.append("?page=");
    builder.append(page);
    builder.append("\"},");

    builder.append("\"first\": { \"href\": \"/ryvrs/");
    builder.append(getTitle());
    builder.append("?page=");
    builder.append(1);
    builder.append("\"},");

    if (page > 1) {
      builder.append("\"prev\": { \"href\": \"/ryvrs/");
      builder.append(getTitle());
      builder.append("?page=");
      builder.append(page - 1L);
      builder.append("\"},");
    }
    if (page < pages) {
      builder.append("\"next\": { \"href\": \"/ryvrs/");
      builder.append(getTitle());
      builder.append("?page=");
      builder.append(page + 1L);
      builder.append("\"},");
    } else {
      builder.append("\"last\": { \"href\": \"/ryvrs/");
      builder.append(getTitle());
      builder.append("?page=");
      builder.append(pages);
      builder.append("\"},");
    }
    builder.append("\"current\": { \"href\": \"/ryvrs/");
    builder.append(getTitle());
    builder.append("\"}}");
    return builder.toString();
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

  @Override
  @JsonIgnore
  public Map<String, List<Map<String, Object>>> getEmbedded() {
    Map<String, List<Map<String, Object>>> rows = new HashMap<>();
    if (rowSet == null) {
      refreshPage(page);
    }
    rowSet.absolute((int) ((page - 1L) * pageSize + 1L));
    for (int i = 0; i < pageSize; ++i) {
      Map<String, Object> row = new HashMap<>();
      for (int j = columnNames.length; j > 0; --j) {
        row.put(columnNames[j - 1], rowSet.getObject(j));
      }
      List<Map<String, Object>> itemRows = rows.get("item");
      if (itemRows == null) {
        itemRows = new ArrayList<Map<String, Object>>(pageSize);
      }
      itemRows.add(row);
      rows.put("item", itemRows);
      if (!rowSet.next()) {
        break;
      }
    }
    return rows;
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

  public void toJsonWithBuilder() {
    StringBuilder builder = new StringBuilder();

    builder.append("{\"title\":\"");
    builder.append(getTitle());
    builder.append("\",\"page\":");
    builder.append(Long.toString(getPage()));

    builder.append(",\"pageSize\":");
    builder.append(Long.toString(getPageSize()));
    builder.append(",\"columns\":[");
    for (int i = 0; i < columnNames.length; ++i) {
      if (i != 0) {
        builder.append(',');
      }
      builder.append('"');
      builder.append(columnNames[i]);
      builder.append('"');
    }
    builder.append("],\"rows\":[");

    for (int i = 0; i < pageSize; ++i) {
      if (i != 0) {
        builder.append(',');
      }
      builder.append('[');
      for (int j = 0; j < columnNames.length; ++j) {
        if (j != 0) {
          builder.append(',');
        }
        String value = rowSet.getString(j + 1);
        if (columnTypes[j] == Types.VARCHAR) {
          builder.append('"');
          builder.append(value);
          builder.append('"');
        } else {
          builder.append(value);
        }
      }
      builder.append(']');
      if (!rowSet.next()) {
        break;
      }
    }
    builder.append(']');
    builder.append('}');
  }
}
