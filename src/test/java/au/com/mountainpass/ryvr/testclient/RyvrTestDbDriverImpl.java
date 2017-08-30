package au.com.mountainpass.ryvr.testclient;

import java.io.FileWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RyvrTestDbDriverImpl implements RyvrTestDbDriver {
  public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private JdbcTemplate currentJt;

  @Autowired
  private DataSource dataSource;

  private Map<String, String> insertSqls = new HashMap<>();
  private Map<String, PreparedStatement> insertStatements = new HashMap<>();

  @Value("${au.com.mountainpass.ryvr.mysql.data.dir:/usr/local/mysql/data/}")
  private String mySqlDataDir;

  @Override
  public String getCatalog(final String dbName) throws Throwable {
    try (Connection connection = currentJt.getDataSource().getConnection()) {
      return currentJt.getDataSource().getConnection().getCatalog();
    }
    // final Connection connection = currentJt.getDataSource().getConnection();
    // connection.getCatalog()
    // final String dbProductName = connection.getMetaData().getDatabaseProductName();
    // LOGGER.info("dbProductName: {}", dbProductName);
    // switch (dbProductName) {
    // case "H2":
    // case "MySQL":
    // currentJt.execute("DROP SCHEMA IF EXISTS `" + dbName + "`;");
    // break;
    // case "PostgreSQL":
    // currentJt.execute("DROP SCHEMA IF EXISTS `" + dbName + "` CASCADE;");
    // break;
    // default:
    // throw new NotImplementedException(dbProductName);
    // }
    // currentJt.execute("CREATE DATABASE `" + dbName + "`");
    // connection.setCatalog(dbName);
    // connection.close();
  }

  @Override
  public void createTable(final String catalog, final String table,
      final Map<String, String> structure) throws Throwable {
    try (final Connection connection = currentJt.getDataSource().getConnection()) {
      connection.setCatalog(catalog);
      String identifierQuoteString = connection.getMetaData().getIdentifierQuoteString();
      currentJt.execute("drop table if exists " + identifierQuoteString + table
          + identifierQuoteString + " CASCADE");

      final StringBuilder statementBuffer = new StringBuilder();
      statementBuffer
          .append("create table " + identifierQuoteString + table + identifierQuoteString + " (");
      boolean first = true;
      for (Entry<String, String> entry : structure.entrySet()) {
        if (!first) {
          statementBuffer.append(", ");
        } else {
          first = false;
        }
        statementBuffer.append(identifierQuoteString + entry.getKey() + identifierQuoteString + " "
            + entry.getValue());
      }
      statementBuffer
          .append(", CONSTRAINT " + identifierQuoteString + "pk_id" + identifierQuoteString
              + " PRIMARY KEY (" + identifierQuoteString + "id" + identifierQuoteString + "))");
      LOGGER.info("TABLE: {}", statementBuffer.toString());
      currentJt.execute(statementBuffer.toString());
      currentJt.update("DELETE FROM " + identifierQuoteString + table + identifierQuoteString);

      first = true;
      String insertSql = "insert into " + identifierQuoteString + table + identifierQuoteString
          + "(";
      for (Entry<String, String> entry : structure.entrySet()) {
        if (!first) {
          insertSql += ", ";
        } else {
          first = false;
        }
        insertSql += identifierQuoteString + entry.getKey() + identifierQuoteString;
      }
      insertSql += ") values (?, ?, ?, ?)";

      PreparedStatement insertStatement = connection.prepareStatement(insertSql);
      insertSqls.put(catalog + '.' + table, insertSql);
      insertStatements.put(catalog + '.' + table, insertStatement);
    }
  }

  @Override
  public void insertRows(final String catalog, final String table,
      final List<Map<String, String>> events) throws Throwable {
    try (final Connection connection = currentJt.getDataSource().getConnection()) {
      final String identifierQuoteString = connection.getMetaData().getIdentifierQuoteString();
      final String dbProductName = connection.getMetaData().getDatabaseProductName();

      switch (dbProductName) {
      case "MySQL":
        if (events.size() > 100) {
          Path path = Files.createTempFile(Paths.get(mySqlDataDir), null, null,
              PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-rw-rw-")));
          path.toFile().deleteOnExit();
          try (Writer writer = new FileWriter(path.toString());) {
            for (Map<String, String> record : events) {
              writer.write(record.get("id"));
              writer.write('\t');
              writer.write(record.get("account"));
              writer.write('\t');
              writer.write(record.get("description"));
              writer.write('\t');
              writer.write(record.get("amount"));
              writer.write("\n");
            }
            writer.flush();
            writer.close();

          }
          String statement = "LOAD DATA LOCAL INFILE '" + path.toString() + "' INTO TABLE `" + table
              + "`(`id`, `account`, `description`, `amount`)";
          LOGGER.info("INSERT STATEMENT: {}", statement);
          currentJt.execute(statement);
          break;
        }
        // else fall through
      default:
        String insertSql = insertSqls.get(catalog + "." + table);
        currentJt.batchUpdate(insertSql, new BatchPreparedStatementSetter() {

          @Override
          public int getBatchSize() {
            return events.size();
          }

          @Override
          public void setValues(final PreparedStatement ps, final int i) throws SQLException {
            // TODO Auto-generated method stub
            final Map<String, String> row = events.get(i);
            ps.setInt(1, Integer.parseInt(row.get("id")));
            ps.setString(2, row.get("account"));
            ps.setString(3, row.get("description"));
            ps.setBigDecimal(4, new BigDecimal(row.get("amount")));
          }

        });
      }
    }

  }

  @Override
  public void insertRow(String catalog, String table, Map<String, String> event) throws Throwable {
    PreparedStatement ps = insertStatements.get(catalog + "." + table);
    ps.setInt(1, Integer.parseInt(event.get("id")));
    ps.setString(2, event.get("account"));
    ps.setString(3, event.get("description"));
    ps.setBigDecimal(4, new BigDecimal(event.get("amount")));
    ps.executeUpdate();
  }

  @Override
  public Map<String, String> adjustConfig(Map<String, String> config) throws Throwable {
    Map<String, String> newConfig = new HashMap<>(config);
    try (Connection connection = currentJt.getDataSource().getConnection()) {
      final String dbProductName = connection.getMetaData().getDatabaseProductName();
      if ("PostgreSQL".equals(dbProductName)) {
        newConfig.put("query", config.get("query").replace('`', '"'));
      }
    }
    return newConfig;
  }

}
